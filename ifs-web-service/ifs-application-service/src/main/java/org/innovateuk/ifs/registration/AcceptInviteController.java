package org.innovateuk.ifs.registration;

import org.innovateuk.ifs.application.service.OrganisationService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.invite.service.InviteRestService;
import org.innovateuk.ifs.registration.viewmodel.ConfirmOrganisationInviteOrganisationViewModel;
import org.innovateuk.ifs.registration.model.AcceptRejectApplicationInviteModelPopulator;
import org.innovateuk.ifs.user.resource.OrganisationResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.innovateuk.ifs.exception.CommonErrorControllerAdvice.URL_HASH_INVALID_TEMPLATE;
import static org.innovateuk.ifs.invite.constant.InviteStatus.SENT;
import static org.innovateuk.ifs.registration.OrganisationCreationController.ORGANISATION_ID;


/**
 * This class is use as an entry point to accept a invite, to a application.
 */
@Controller
@PreAuthorize("permitAll")
public class AcceptInviteController extends AbstractAcceptInviteController {

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private InviteRestService inviteRestService;

    @Autowired
    private AcceptRejectApplicationInviteModelPopulator acceptRejectApplicationInviteModelPopulator;

    private static final String ACCEPT_INVITE_NEW_USER_VIEW = "registration/accept-invite-new-user";
    private static final String ACCEPT_INVITE_EXISTING_USER_VIEW = "registration/accept-invite-existing-user";

    @GetMapping("/accept-invite/{hash}")
    public String inviteEntryPage(
            @PathVariable("hash") final String hash,
            UserResource loggedInUser,
            HttpServletResponse response,
            Model model) {
        RestResult<String> view = inviteRestService.getInviteByHash(hash).andOnSuccess(invite ->
                inviteRestService.getInviteOrganisationByHash(hash).andOnSuccessReturn(inviteOrganisation -> {
                            if (!SENT.equals(invite.getStatus())) {
                                return alreadyAcceptedView(response);
                            }
                            if (loggedInAsNonInviteUser(invite, loggedInUser)) {
                                return LOGGED_IN_WITH_ANOTHER_USER_VIEW;
                            }
                            // Success
                            putInviteHashCookie(response, invite.getHash()); // Add the hash to a cookie for later flow lookup.
                            model.addAttribute("model", acceptRejectApplicationInviteModelPopulator.populateModel(invite, inviteOrganisation));
                            return invite.getUser() == null ? ACCEPT_INVITE_NEW_USER_VIEW : ACCEPT_INVITE_EXISTING_USER_VIEW;
                        }
                )
        ).andOnFailure(clearDownInviteFlowCookiesFn(response));
        return view.getStatusCode().is4xxClientError() ? URL_HASH_INVALID_TEMPLATE : view.getSuccessObject();
    }

    @GetMapping("/accept-invite/confirm-invited-organisation")
    public String confirmInvitedOrganisation(HttpServletResponse response,
                                             HttpServletRequest request,
                                             UserResource loggedInUser,
                                             Model model) {
        String hash = getInviteHashCookie(request);
        RestResult<String> view = inviteRestService.getInviteByHash(hash).andOnSuccess(invite ->
                inviteRestService.getInviteOrganisationByHash(hash).andOnSuccessReturn(inviteOrganisation -> {
                            if (!SENT.equals(invite.getStatus())) {
                                return alreadyAcceptedView(response);
                            }
                            if (loggedInAsNonInviteUser(invite, loggedInUser)) {
                                return LOGGED_IN_WITH_ANOTHER_USER_VIEW;
                            }
                            OrganisationResource organisation = organisationService.getOrganisationByIdForAnonymousUserFlow(inviteOrganisation.getOrganisation());
                            cookieUtil.saveToCookie(response, ORGANISATION_ID, String.valueOf(inviteOrganisation.getOrganisation()));
                            model.addAttribute("model",
                                    new ConfirmOrganisationInviteOrganisationViewModel(invite, organisation,
                                            getOrganisationAddress(organisation), RegistrationController.BASE_URL));
                            return "registration/confirm-invited-organisation";
                        }
                )
        ).andOnFailure(clearDownInviteFlowCookiesFn(response));
        return view.getSuccessObject();
    }
}
