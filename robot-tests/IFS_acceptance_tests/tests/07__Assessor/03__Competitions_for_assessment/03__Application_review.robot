*** Settings ***
Documentation     INFUND-3780: As an Assessor I want the system to autosave my work so that I can be sure that my assessment is always in its most current state.
...
...               INFUND-3303: As an Assessor I want the ability to reject the application after I have been given access to the full details so I can make Innovate UK aware.
...
...               INFUND-4203: Prevent navigation options appearing for questions that are not part of an assessment
...
...               INFUND-1483: As an Assessor I want to be asked to confirm whether the application is in the correct research category and scope so that Innovate UK know that the application aligns with the competition
...
...               INFUND-3394 Acceptance Test: Assessor should be able to view the full application and finance summaries for assessment
...
...               INFUND-3859: As an Assessor I want to see how many words I can enter as feedback so that I know how much I can write.
...
...               INFUND-6281 As an Assessor I want to see specific scoring guidance text for each application question so that I can score the question accurately
...
...               INFUND-8065 File download links are broken for assessors
Suite Setup       The user logs-in in new browser  &{assessor_credentials}
Suite Teardown    the user closes the browser
Force Tags        Assessor
Resource          ../../../resources/defaultResources.robot

*** Test Cases ***
Navigation using previous button
    [Documentation]    INFUND-4264
    [Tags]    HappyPath
    Given the user clicks the button/link              link=${IN_ASSESSMENT_COMPETITION_NAME}
    And The user clicks the button/link                link=Products and Services Personalised
    When the user clicks the button/link               link=4. Economic benefit
    Then the user should see the text in the page      Economic benefit
    And the user clicks previous and goes to the page  Project exploitation
    And the user clicks previous and goes to the page  Potential market
    And the user clicks previous and goes to the page  Business opportunity
    And the user clicks previous and goes to the page  Scope
    And the user clicks previous and goes to the page  Public description
    And the user clicks previous and goes to the page  Project summary
    And the user clicks previous and goes to the page  Application details
    And the user should not see the element            css=.prev

Project details sections should not be scorable
    [Documentation]    INFUND-3400 INFUND-4264
    [Tags]
    When the user clicks the button/link               link=Back to your assessment overview
    And the user clicks the button/link                link=Application details
    And the user should see the text in the page       Project title
    Then the user should not see the text in the page  Question score
    When the user clicks the button/link               jQuery=span:contains("Next")
    And the user should see the text in the page       This is the applicant response for project summary.
    Then the user should not see the text in the page  Question score
    When the user clicks the button/link               jQuery=span:contains("Next")
    And the user should see the text in the page       This is the applicant response for public description.
    Then the user should not see the text in the page  Question score
    And the user clicks the button/link                jQuery=span:contains("Next")
    And the user should see the text in the page       This is the applicant response for how does your project align with the scope of this competition?.
    Then the user should not see the text in the page  Question score

Application questions should be scorable
    [Documentation]    INFUND-3400 INFUND-4264
    [Tags]
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  What is the business opportunity that your project addresses?
    And the user should see the text in the page  This is the applicant response for what is the business opportunity that your project addresses?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  What is the size of the potential market for your project
    And the user should see the text in the page  This is the applicant response for what is the size of the potential market for your project?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  How will you exploit and market your project?
    And the user should see the text in the page  This is the applicant response for how will you exploit and market your project?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  What economic, social and environmental benefits do you expect
    And the user should see the text in the page  This is the applicant response for what economic, social and environmental benefits do you expect your project to deliver and when?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  What technical approach will you use and how will you manage your project?
    And the user should see the text in the page  This is the applicant response for what technical approach will you use and how will you manage your project?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  What is innovative about your project
    And the user should see the text in the page  This is the applicant response for what is innovative about your project?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  What are the risks
    And the user should see the text in the page  This is the applicant response for what are the risks (technical, commercial and environmental) to your project's success? what is your risk management strategy?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  Does your project team have the skills,
    And the user should see the text in the page  This is the applicant response for does your project team have the skills, experience and facilities to deliver this project?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  What will your project cost
    And the user should see the text in the page  This is the applicant response for what will your project cost?.
    Then The user should see the element          jQuery=label:contains("Question score")
    When the user clicks the button/link          jQuery=span:contains("Next")
    And The user should see the text in the page  How does financial support from Innovate UK
    And the user should see the text in the page  This is the applicant response for how does financial support from innovate uk and its funding partners add value?.
    Then The user should see the element          jQuery=label:contains("Question score")
    [Teardown]  the user clicks the button/link    link=Back to your assessment overview

Appendix can be opened on the question view
    [Documentation]    INFUND-8065
    [Tags]
    Given The user opens the link in new window  products-and-services-personalised-technical-approach.pdf, 7 KB
    And The user opens the link in new window    products-and-services-personalised-innovation.pdf, 7 KB
    And The user opens the link in new window    products-and-services-personalised-project-team.pdf, 7 KB
    When the user clicks the button/link         jQuery=a:contains("6. Innovation")
    And The user opens the link in new window    products-and-services-personalised-innovation.pdf, 7.94 KB
    And the user goes back to the previous tab

Scope: Validations
    [Documentation]  IFS-508
    [Tags]
    Given the user clicks the button/link               link=Back to your assessment overview
    And the user clicks the button/link                 link=Scope
    When the user clicks the button/link                jQuery=button:contains("Save and return to assessment overview")
    Then the user should see a field and summary error  Please select a research category.

Scope: Status in the overview is updated
    [Documentation]    INFUND-1483
    [Tags]    HappyPath
    Given the user clicks the button/link                    link=Back to your assessment overview
    And the user clicks the button/link                      link=Scope
    When the user selects the index from the drop-down menu  1    id=research-category
    And the user clicks the button/link                      jQuery=label:contains("Yes")
    And The user enters text to a text field                 css=.editor    Testing feedback field when "Yes" is selected.
    And the user clicks the button/link                      jquery=button:contains("Save and return to assessment overview")
    And the user should see the text in the page             In scope
    And the user should see the element                      css=.task-status-complete

Scope: Autosave
    [Documentation]    INFUND-1483
    ...
    ...    INFUND-3780
    [Tags]    HappyPath
    When the user clicks the button/link          link=Scope
    And the user should see the text in the page  Feasibility studies
    And the user should see the text in the page  Testing feedback field when "Yes" is selected.

Scope: Word count
    [Documentation]    INFUND-1483
    ...
    ...    INFUND-3400
    [Tags]    HappyPath
    When the user enters multiple strings into a text field  css=.editor  a${SPACE}  100
    Then the user should see the text in the page            Words remaining: 0

Scope: Guidance
    [Documentation]    INFUND-4142
    ...
    ...    INFUND-6281
    [Tags]    HappyPath
    When the user clicks the button/link          css=details summary
    Then the user should see the element          css=#details-content-0
    And The user should see the text in the page  One or more of the above requirements have not been satisfied.
    And The user should see the text in the page  Does it meet the scope of the competition as defined in the competition brief?
    And the user clicks the button/link           css=details summary
    And The user should not see the element       css=#details-content-0

Economic Benefit: validations
    [Documentation]  IFS-508
    [Tags]
    Given the user clicks the button/link               link=Back to your assessment overview
    And I open one of the application questions         link=4. Economic benefit
    When the user clicks the button/link                jQuery=button:contains("Save and return to assessment overview")
    Then the user should see a field and summary error  The assessor score must be a number.

Economic Benefit: word count
    [Documentation]    INFUND-3859
    [Tags]
    [Setup]    The user clicks the button/link             link=Back to your assessment overview
    Given I open one of the application questions          link=4. Economic benefit
    And I should see word count underneath feedback form   Words remaining: 100
    When I enter feedback of words                         102
    Then the user should see a summary error               Maximum word count exceeded. Please reduce your word count to 100.
    When I enter feedback of words                         10
    Then I should see word count underneath feedback form  Words remaining: 90
    And the user should not see an error in the page

Economic Benefit: Autosave
    [Documentation]    INFUND-3780
    [Tags]
    When the user selects the option from the drop-down menu  9    id=assessor-question-score
    And the user enters text to a text field                  css=.editor    This is to test the feedback entry.
    And the user clicks the button/link                       jQuery=a:contains(Back to your assessment overview)
    And the user clicks the button/link                       link=4. Economic benefit
    Then the user should see the text in the page             This is to test the feedback entry.
    And the user should see the text in the page              9

Economic Benefit: Guidance
    [Documentation]    INFUND-6281
    When The user clicks the button/link           css=.summary
    Then the user should see the text in the page  The project is damaging to other stakeholders with no realistic mitigation or balance described.
    And The user should see the text in the page   The project has no outside benefits or is potentially damaging to other stakeholders. No mitigation or exploitation is suggested.
    And The user should see the text in the page   Some positive outside benefits are described but the methods to exploit these are not obvious. Or the project is likely to have a negative impact but some mitigation or a balance against the internal benefits is proposed.
    And The user should see the text in the page   Some positive outside benefits are defined and are realistic. Methods of addressing these opportunities are described.
    And The user should see the text in the page   Inside and outside benefits are well defined, realistic and of significantly positive economic, environmental or social impact. Routes to exploit these benefits are also provided.
    [Teardown]  The user clicks the button/link    link=Back to your assessment overview

Finance overview
    [Documentation]    INFUND-3394
    [Tags]
    When the user clicks the button/link           link=Finances overview
    Then the user should see the text in the page  Finances summary
    And the finance summary total should be correct
    And the project cost breakdown total should be correct

Status of the application should be In Progress
    [Documentation]    INFUND-6358
    [Tags]
    [Setup]    The user navigates to the page      ${assessor_dashboard_url}
    When The user clicks the button/link           link=${IN_ASSESSMENT_COMPETITION_NAME}
    Then The user should see the text in the page  In progress

*** Keywords ***
the user clicks next and goes to the page
    [Arguments]    ${page_content}
    the user clicks the button/link           css=.next
    the user should see the text in the page  ${page_content}

I enter feedback of words
    [Arguments]    ${no_of_words}
    the user enters multiple strings into a text field  css=.editor  a${SPACE}  ${no_of_words}

I should see word count underneath feedback form
    [Arguments]    ${wordCount}
    the user should see the text in the page  ${wordCount}

I should see validation message above the feedback form text field
    [Arguments]    ${error_message}
    the user should see the text in the page  ${error_message}

I should not see validation message above the feedback form text field
    [Arguments]    ${error_message}
    the user should not see the text in the page  ${error_message}

I open one of the application questions
    [Arguments]    ${application_question}
    the user clicks the button/link  ${application_question}

the user clicks previous and goes to the page
    [Arguments]    ${page_content}
    the user clicks the button/link           css=.prev
    the user should see the text in the page  ${page_content}

the finance summary total should be correct
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(1) td:nth-child(2)    £200,903
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(1) td:nth-child(3)    30%
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(1) td:nth-child(4)    £57,803
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(1) td:nth-child(5)    £2,468
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(1) td:nth-child(6)    £140,632
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(2) td:nth-child(2)    £200,903
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(2) td:nth-child(4)    £57,803
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(2) td:nth-child(5)    £2,468
    Element Should Contain    css=.form-group.finances-summary tbody tr:nth-child(2) td:nth-child(6)    £140,632

the project cost breakdown total should be correct
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(2)    £200,903
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(3)    £3,081
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(4)    £0
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(5)    £100,200
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(6)    £552
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(7)    £90,000
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(8)    £5,970
    Element Should Contain    css=.project-cost-breakdown tbody tr:nth-child(1) td:nth-child(9)    £1,100

The status of the appllications should be correct
    [Arguments]    ${APPLICATION}    ${STATUS}
    element should contain    ${APPLICATION}    ${STATUS}
