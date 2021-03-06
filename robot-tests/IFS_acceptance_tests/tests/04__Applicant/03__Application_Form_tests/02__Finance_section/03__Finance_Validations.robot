*** Settings ***
Documentation     INFUND-844: As an applicant I want to receive a validation error in the finance sections if I my input is invalid in a particular field so that I am informed how to correctly submit the information
...
...               INFUND-2214: As an applicant I want to be prevented from marking my finances as complete if I have not fully completed the Other funding section so that I can be sure I am providing all the required information
Suite Setup       log in and create new application if there is not one already with complete application details
Suite Teardown    mark application details incomplete the user closes the browser
Force Tags        Applicant  Pending
Resource          ../../../../resources/defaultResources.robot
Resource          ../../Applicant_Commons.robot

*** Test Cases ***
Mark as complete Your funding with only one input should not be possible
    [Documentation]    INFUND-2214
    [Tags]
    [Setup]  Applicant navigates to the finances of the Robot application
    When the user clicks the button/link      link=Your funding
    And the user enters text to a text field  css=#cost-financegrantclaim  80
    And the user moves focus to the element   jQuery=[data-target="other-funding-table"] label
    Then the user should see the element      jQuery=.button[disabled]:contains("Mark as complete")

Other funding client side
    [Documentation]    INFUND-2214
    [Tags]
    [Setup]  Applicant navigates to the finances of the Robot application
    Given the user clicks the button/link  link=Your funding
    And the user clicks the button/link    jQuery=label:contains("Yes")
    Then the user should see the element   css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    When the user enters invalid inputs in the other funding fields    ${EMPTY}    132020    -6565
    Then the user gets the expected validation errors    Invalid secured date.    Funding source cannot be blank.
    And the user moves focus to the element    jQuery=label:contains("Yes")
    And the user should see an error    This field should be 1 or higher.

Other funding server side
    [Documentation]    INFUND-2214
    [Tags]
    [Setup]
    When the user enters invalid inputs in the other funding fields    ${EMPTY}    13-2020    -6565
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    Funding source cannot be blank.
    And the user should see an error    Please use MM-YYYY format.
    And the user should see an error    This field should be 1 or higher.
    And the user should see the element    css=.error-summary
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    Funding source cannot be blank.
    And the user should see an error    This field cannot be left blank.
    And the user should see an error    This field should be 1 or higher.

Select NO Other Funding and mark as complete should be possible
    [Documentation]    INFUND-2214
    [Tags]
    Given the user enters text to a text field  css=#cost-financegrantclaim  50
    When the user clicks the button/link        jQuery=label:contains("No")
    And the user selects the checkbox           agree-terms-page
    Then the user clicks the button/link        jQuery=.button:contains("Mark as complete")
    And the user should not see an error in the page

Labour client side
    [Documentation]    INFUND-844
    [Tags]
    [Setup]  Applicant navigates to the finances of the Robot application
    And the user clicks the button/link  link=Your project costs
    Given the user clicks the button/link    jQuery=button:contains("Labour")
    When the user enters text to a text field    css=[name^="labour-labourDaysYearly"]    -1
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    Then the user should see an error           This field should be 1 or higher.
    When the user enters text to a text field    css=[name^="labour-labourDaysYearly"]    366
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input    12121212121212121212121212
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(4) input    123456789101112
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    Then the user gets the expected validation errors    You must enter a value less than 10 digits.    You must enter a value less than 20 digits.
    And the user gets the expected validation errors    This field should be 365 or lower.    This field cannot be left blank.
    When the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input    123456789101112131415161718192021
    When the user enters text to a text field    css=[name^="labour-labourDaysYearly"]    120
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(4) input    -1
    Then the user gets the expected validation errors    You must enter a value less than 20 digits.    This field should be 1 or higher.
    [Teardown]

Labour server side
    [Documentation]    INFUND-844
    [Tags]
    When the user enters text to a text field    css=[name^="labour-labourDaysYearly"]    366
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(2) input    ${EMPTY}
    And the user enters text to a text field    css=.labour-costs-table tr:nth-of-type(1) td:nth-of-type(4) input    -1
    And the user clicks the button/link         jQuery=.button:contains("Mark as complete")
    Then the user should see an error       This field should be 1 or higher.
    And the user should see an error        This field cannot be left blank.
    Then the user should see the element    jQuery=.error-summary li:contains("This field should be 365 or lower")
    And the user should see the element     jQuery=.error-summary li:contains("This field should be 1 or higher")
    And the user should see the element     jQuery=.error-summary li:contains("This field cannot be left blank")
    And the user should see the element    css=.error-summary
    [Teardown]    Run keywords    the user enters text to a text field    css=[name^="labour-labourDaysYearly"]    21
    ...    AND    Remove row    jQuery=button:contains("Labour")    jQuery=.labour-costs-table button:contains("Remove")

Admin costs client side
    [Documentation]    INFUND-844
    Given the user clicks the button/link    jQuery=button:contains("Overhead costs")
    Given the user clicks the button/link    jQuery=label:contains("Custom overhead costs")
    And the user enters text to a text field    css=[id$="customRate"]    ${EMPTY}
    Then the user gets the expected validation errors    This field cannot be left blank    This field cannot be left blank.    #Entered two times the same error because this keyword expects two errors
    When the user enters text to a text field    css=[id$="customRate"]    101
    Then the user gets the expected validation errors    This field should be 100 or lower    This field should be 100 or lower.    #Entered two times the same error because this keyword expects two errors
    When the user enters text to a text field    css=[id$="customRate"]    12121212121212121212121212
    Then the user gets the expected validation errors    This field should be 100 or lower    This field should be 100 or lower.    #Entered two times the same error because this keyword expects two errors
    When the user enters text to a text field    css=[id$="customRate"]    -1
    Then the user gets the expected validation errors    This field should be 1 or higher    This field should be 1 or higher.    #Entered two times the same error because this keyword expects two errors

Admin costs server side
    [Documentation]    INFUND-844
    [Tags]
    When the user enters text to a text field    css=[id$="customRate"]    ${EMPTY}
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field should be 1 or higher.
    And the user enters text to a text field    css=[id$="customRate"]    101
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field should be 100 or lower.
    And the user should see the element    css=.error-summary
    And the user enters text to a text field    css=[id$="customRate"]    -1
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field should be 1 or higher.
    And the user should see the element    css=.error-summary
    [Teardown]    Run keywords    Given the user clicks the button/link    jQuery=label:contains("20% of labour costs")
    ...    AND    the user clicks the button/link    jQuery=button:contains("Overhead costs")

Materials client side
    [Documentation]    INFUND-844
    [Tags]    HappyPath
    Given the user clicks the button/link    jQuery=button:contains("Materials")
    When the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    1234567810111213141516171819202122
    And the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    -1
    the user moves focus to the element    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(4) input
    Then the user gets the expected validation errors    You must enter a value less than 10 digits.    This field should be 1 or higher.
    [Teardown]

Materials server side
    [Documentation]    INFUND-844
    [Tags]    HappyPath
    When the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    And the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    -1
    And the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    1212121212121212121212
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field cannot be left blank.
    And the user should see an error    You must enter a value less than 20 digits.
    And the user should see an error    This field should be 1 or higher.
    And the user should see the element    css=.error-summary
    When the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    And the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    1
    And the user enters text to a text field    css=#material-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    -1
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field cannot be left blank.
    And the user should see an error    This field should be 1 or higher.
    And the user should see the element    css=.error-summary
    [Teardown]    Remove row    jQuery=button:contains("Material")    jQuery=#material-costs-table button:contains("Remove")

Capital usage client side
    [Documentation]    INFUND-844
    Given the user clicks the button/link    jQuery=button:contains("Capital usage")
    When the user enters text to a text field    css=.form-finances-capital-usage-depreciation    ${EMPTY}
    And the user enters text to a text field    css=.form-row:nth-child(1) .form-finances-capital-usage-residual-value    12121212121212121212121212121
    And the user enters text to a text field    css=.form-row:nth-child(1) .form-finances-capital-usage-npv    -1
    And the user enters text to a text field    css=.form-finances-capital-usage-utilisation    101
    Then the user gets the expected validation errors    This field should be 1 or higher.    You must enter a value less than 20 digits.
    And the user gets the expected validation errors    This field cannot be left blank.    This field should be 100 or lower.
    When the user enters text to a text field    css=.form-finances-capital-usage-depreciation    12121212121212121212121212121
    And the user enters text to a text field    css=.form-row:nth-child(1) .form-finances-capital-usage-residual-value    -1
    And the user enters text to a text field    css=.form-row:nth-child(1) .form-finances-capital-usage-npv    -1
    And the user enters text to a text field    css=.form-finances-capital-usage-utilisation    101
    Then the user gets the expected validation errors    You must enter a value less than 10 digits.    This field should be 1 or higher.
    And the user gets the expected validation errors    This field should be 0 or higher.    This field should be 100 or lower.

Capital usage server side
    [Documentation]    INFUND-844
    [Tags]
    When the user enters text to a text field    css=.form-row:nth-child(1) .form-finances-capital-usage-npv    -1
    And the user enters text to a text field    css=.form-row:nth-child(1) .form-finances-capital-usage-residual-value    -2
    And the user enters text to a text field    css=.form-finances-capital-usage-utilisation    -1
    And the user enters text to a text field    css=.form-finances-capital-usage-depreciation    ${EMPTY}
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field cannot be left blank.
    And the user should see an error    This field cannot be left blank.
    And the user should see an error    This field should be 1 or higher.
    And the user should see an error    This field should be 0 or higher.
    And the user should see the element    css=.error-summary
    [Teardown]    Remove row    jQuery=button:contains("Capital usage")    jQuery=#capital_usage button:contains("Remove")

Subcontracting costs client side
    [Documentation]    INFUND-844
    Given the user clicks the button/link    jQuery=button:contains("Subcontracting costs")
    When the user enters text to a text field    css=#collapsible-4 .form-row:nth-child(1) input[id$=subcontractingCost]    ${EMPTY}
    And the user enters text to a text field    css=#collapsible-4 .form-row:nth-child(1) input[id$=name]    ${EMPTY}
    Then the user gets the expected validation errors    This field cannot be left blank.    This field should be 1 or higher.

Subcontracting costs server side
    [Documentation]    INFUND-844
    [Tags]
    When the user enters text to a text field    css=#collapsible-4 .form-row:nth-child(1) input[id$=subcontractingCost]    -100
    And the user enters text to a text field    css=#collapsible-4 .form-row:nth-child(1) input[id$=name]    ${EMPTY}
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field should be 1 or higher.
    And the user should see an error    This field cannot be left blank.
    And the user should see the element    css=.error-summary
    [Teardown]    Remove row    jQuery=button:contains("Subcontracting")    jQuery=#subcontracting button:contains("Remove")

Travel and subsistence client side
    [Documentation]    INFUND-844
    Given the user clicks the button/link    jQuery=button:contains("Travel and subsistence")
    When the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    And the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    0123456789101112131415161718192021
    And the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    -1
    Then the user gets the expected validation errors    This field cannot be left blank.    You must enter a value less than 10 digits.
    And the user should see an error    This field should be 1 or higher.
    When the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    And the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    0
    And the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    13123232134234234234234234423
    Then the user gets the expected validation errors    This field cannot be left blank.    You must enter a value less than 20 digits.
    And the user should see an error    This field should be 1 or higher.

Travel and subsistence server side
    [Documentation]    INFUND-844
    [Tags]
    When the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${EMPTY}
    And the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    -1
    And the user enters text to a text field    css=#travel-costs-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    0123456789101112131415161718192021
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field cannot be left blank.
    And the user should see an error    This field should be 1 or higher.
    And the user should see an error    You must enter a value less than 20 digits.
    And the user should see the element    css=.error-summary
    [Teardown]    Remove row    jQuery=button:contains("Travel")    jQuery=#travel-costs-table button:contains("Remove")

Other costs client side
    [Documentation]    INFUND-844
    Given the user clicks the button/link    jQuery=button:contains("Other costs")
    When the user enters text to a text field    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    ${EMPTY}
    And the user enters text to a text field    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) textarea    ${EMPTY}
    Then the user gets the expected validation errors    This field cannot be left blank.    This field should be 1 or higher.
    When the user enters text to a text field    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    -1
    And the user enters text to a text field    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) textarea    ${EMPTY}
    Then the user gets the expected validation errors    This field cannot be left blank.    This field should be 1 or higher.

Save with validation errors
    [Documentation]    INFUND-844
    [Tags]
    When the user reloads the page with validation errors
    Then the field with the wrong input should be saved

Other costs server side
    [Documentation]    INFUND-844
    [Tags]
    When the user enters text to a text field    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    -1
    And the user enters text to a text field    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(1) textarea    ${EMPTY}
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field should be 1 or higher.
    And the user should see an error    This field cannot be left blank.
    And the user should see the element    css=.error-summary
    [Teardown]    Remove row    jQuery=button:contains("Other costs")    jQuery=#other-costs-table button:contains("Remove")

#Funding level client side is covered in 02__Org_size_validation.robot

Funding level server side
    [Documentation]    INFUND-844
    [Tags]
    When the user enters text to a text field    css=#cost-financegrantclaim  61
    And the user clicks the button/link  jQuery=.button:contains("Mark as complete")
    Then the user should see an error    This field should be 60% or lower.
    Then the user enters text to a text field    css=#cost-financegrantclaim  59
    And the user moves focus to the element      jQuery=[data-target="other-funding-table"] label
    And the user should not see the element      jQuery=.error-message

*** Keywords ***
user selects the admin costs
    [Arguments]    ${RADIO_BUTTON}    ${SELECTION}
    the user selects the radio button    ${RADIO_BUTTON}    ${SELECTION}
    focus    css=.app-submit-btn

the field with the wrong input should be saved
    the user should see the element    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    ${input_value} =    Get Value    css=#other-costs-table tbody tr:nth-of-type(1) td:nth-of-type(2) input
    Should Be Equal As Strings    ${input_value}    -1

the user reloads the page with validation errors
    the user moves focus to the element  jQuery=.button:contains("Mark as complete")
    wait for autosave
    the user reloads the page
    wait for autosave

the user enters invalid inputs in the other funding fields
    [Arguments]    ${SOURCE}    ${DATE}    ${FUNDING}
    the user enters text to a text field    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(1) input    ${SOURCE}
    the user enters text to a text field    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(2) input    ${DATE}
    the user enters text to a text field    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(3) input    ${FUNDING}
    the user moves the mouse away from the element    css=#other-funding-table tbody tr:nth-of-type(1) td:nth-of-type(3) input
    the user moves focus to the element     jQuery=.button:contains("Mark as complete")

Remove row
    [Arguments]    ${section}    ${close button}
    the user moves focus to the element    ${close button}
    wait for autosave
    the user clicks the button/link    ${close button}
    the user clicks the button/link    ${section}

The user gets the expected validation errors
    [Arguments]    ${ERROR1}    ${ERROR2}
    the user moves focus to the element    jQuery=.button:contains("Mark as complete")
    wait for autosave
    Then the user should see an error    ${ERROR1}
    And the user should see an error    ${ERROR2}

The user enters the funding level
    The applicant enters Org Size and Funding level  ${SMALL_ORGANISATION_SIZE}  20
