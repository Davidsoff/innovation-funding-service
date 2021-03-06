package org.innovateuk.ifs.dashboard.controller;

import org.innovateuk.ifs.dashboard.populator.ApplicantDashboardPopulator;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This controller will handle requests related to the current applicant. So pages that are relative to that user,
 * are implemented here. For example the my-applications page.
 */
@Controller
@RequestMapping("/applicant")
@PreAuthorize("hasAuthority('applicant')")
public class ApplicantController {

    @Autowired
    private ApplicantDashboardPopulator applicantDashboardPopulator;

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            UserResource user) {

        model.addAttribute("model", applicantDashboardPopulator.populate(user));

        return "applicant-dashboard";
    }
}
