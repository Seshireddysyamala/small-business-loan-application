const API_URL = "/api/companies";
const LAST_APPLICATION_KEY = "lastLoanApplicationId";

const getElement = (id) => document.getElementById(id);

const companyForm = getElement("companyForm");
const companyId = getElement("companyId");
const formMode = getElement("formMode");
const formTitle = getElement("formTitle");
const applicationStatus = getElement("applicationStatus");
const saveButton = getElement("saveButton");
const cancelEditButton = getElement("cancelEditButton");
const statusMessage = getElement("statusMessage");

const lookupForm = getElement("lookupForm");
const lookupId = getElement("lookupId");
const lookupMessage = getElement("lookupMessage");
const resumeLastButton = getElement("resumeLastButton");

const confirmationPanel = getElement("confirmationPanel");
const confirmationId = getElement("confirmationId");
const confirmationSummary = getElement("confirmationSummary");
const editApplicationButton = getElement("editApplicationButton");
const startNewButton = getElement("startNewButton");

const ISO_COUNTRY_CODES = (
    "AD AE AF AG AI AL AM AO AQ AR AS AT AU AW AX AZ "
    + "BA BB BD BE BF BG BH BI BJ BL BM BN BO BQ BR BS BT BV BW BY BZ "
    + "CA CC CD CF CG CH CI CK CL CM CN CO CR CU CV CW CX CY CZ "
    + "DE DJ DK DM DO DZ EC EE EG EH ER ES ET FI FJ FK FM FO FR "
    + "GA GB GD GE GF GG GH GI GL GM GN GP GQ GR GS GT GU GW GY "
    + "HK HM HN HR HT HU ID IE IL IM IN IO IQ IR IS IT JE JM JO JP "
    + "KE KG KH KI KM KN KP KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY "
    + "MA MC MD ME MF MG MH MK ML MM MN MO MP MQ MR MS MT MU MV MW MX MY MZ "
    + "NA NC NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PN PR PS PT PW PY "
    + "QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK SL SM SN SO SR SS ST SV SX SY SZ "
    + "TC TD TF TG TH TJ TK TL TM TN TO TR TT TV TW TZ UA UG UM US UY UZ "
    + "VA VC VE VG VI VN VU WF WS YE YT ZA ZM ZW"
).split(" ");
const ONLINE_RESTRICTED_COUNTRIES = new Set(["CU", "IR", "KP"]);

const fields = {
    companyName: getElement("companyName"),
    businessStructure: getElement("businessStructure"),
    industry: getElement("industry"),
    countryOfRegistration: getElement("countryOfRegistration"),
    yearsInBusiness: getElement("yearsInBusiness"),
    annualRevenue: getElement("annualRevenue"),
    totalAssets: getElement("totalAssets"),
    loanType: getElement("loanType"),
    requestedLoanAmount: getElement("requestedLoanAmount"),
    requestedTermMonths: getElement("requestedTermMonths"),
    loanPurpose: getElement("loanPurpose"),
    creditScore: getElement("creditScore"),
    contactName: getElement("contactName"),
    ssn: getElement("ssn"),
    contactEmail: getElement("contactEmail"),
    contactPhone: getElement("contactPhone")
};

const numericFields = new Set([
    "yearsInBusiness",
    "annualRevenue",
    "totalAssets",
    "requestedLoanAmount",
    "requestedTermMonths",
    "creditScore"
]);

let currentApplicationId = null;

companyForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    if (!companyForm.reportValidity()) {
        return;
    }

    const id = companyId.value;
    const requestUrl = id ? `${API_URL}/${id}` : API_URL;
    const requestMethod = id ? "PUT" : "POST";

    setFormBusy(true, Boolean(id));
    showStatus(id ? "Saving your changes…" : "Submitting your application…");

    try {
        const response = await fetch(requestUrl, {
            method: requestMethod,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(buildApplicationPayload())
        });

        if (!response.ok) {
            throw new Error(await readError(response));
        }

        const application = await response.json();

        rememberApplication(application.id);
        showConfirmation(application, Boolean(id));
    } catch (error) {
        showStatus(error.message, "error");
    } finally {
        setFormBusy(false, Boolean(id));
    }
});

lookupForm.addEventListener("submit", async function (event) {
    event.preventDefault();

    const id = Number(lookupId.value);

    if (!Number.isInteger(id) || id < 1) {
        showLookupMessage("Enter a valid application number.", "error");
        return;
    }

    await loadApplication(id);
});

resumeLastButton.addEventListener("click", async function () {
    const id = Number(resumeLastButton.dataset.applicationId);

    if (Number.isInteger(id) && id > 0) {
        lookupId.value = id;
        await loadApplication(id);
    }
});

editApplicationButton.addEventListener("click", async function () {
    if (currentApplicationId) {
        await loadApplication(currentApplicationId);
    }
});

startNewButton.addEventListener("click", function () {
    resetApplicationForm();
    companyForm.scrollIntoView({ behavior: "smooth", block: "start" });
});

cancelEditButton.addEventListener("click", function () {
    resetApplicationForm();
    showStatus("");
});

function buildApplicationPayload() {
    return Object.fromEntries(
        Object.entries(fields).map(function ([name, input]) {
            const value = input.value.trim();

            return [name, numericFields.has(name) ? Number(value) : value];
        })
    );
}

async function loadApplication(id) {
    showLookupMessage("Retrieving application…");

    try {
        const response = await fetch(`${API_URL}/${id}`);

        if (!response.ok) {
            throw new Error(await readError(response));
        }

        const application = await response.json();

        populateApplicationForm(application);
        rememberApplication(application.id);
        showLookupMessage(`Application #${application.id} is ready to review.`, "success");
        companyForm.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        showLookupMessage(error.message, "error");
    }
}

function populateApplicationForm(application) {
    Object.entries(fields).forEach(function ([name, input]) {
        if (name === "ssn") {
            input.value = "";
            input.placeholder = application.ssnLastFour
                ? `Re-enter SSN ending in ${application.ssnLastFour}`
                : "123-45-6789";
            return;
        }

        input.value = application[name] ?? "";
    });

    getElement("certifyConsent").checked = false;
    companyId.value = application.id;
    currentApplicationId = application.id;

    formMode.textContent = `Application #${application.id}`;
    formTitle.textContent = "Review your loan request";
    applicationStatus.textContent = formatStatus(application.applicationStatus);
    applicationStatus.classList.add("submitted");
    saveButton.textContent = "Save application changes";
    cancelEditButton.textContent = "Cancel changes";

    confirmationPanel.classList.add("hidden");
    companyForm.classList.remove("hidden");
    showStatus("Review every section, certify the information, then save your changes.");
}

function showConfirmation(application, wasUpdated) {
    currentApplicationId = application.id;
    confirmationId.textContent = application.id;
    confirmationSummary.textContent = `${application.companyName} requested ${formatCurrency(
        application.requestedLoanAmount
    )} in ${application.loanType.toLowerCase()} financing.`;

    getElement("confirmationTitle").textContent = wasUpdated
        ? "Your application has been updated."
        : "Your request has been submitted.";

    companyForm.classList.add("hidden");
    confirmationPanel.classList.remove("hidden");
    confirmationPanel.scrollIntoView({ behavior: "smooth", block: "start" });
}

function resetApplicationForm() {
    companyForm.reset();
    fields.ssn.placeholder = "123-45-6789";
    fields.countryOfRegistration.value = "US";
    companyId.value = "";
    currentApplicationId = null;

    formMode.textContent = "New application";
    formTitle.textContent = "Business loan request";
    applicationStatus.textContent = "Not submitted";
    applicationStatus.classList.remove("submitted");
    saveButton.textContent = "Submit application";
    cancelEditButton.textContent = "Clear form";

    confirmationPanel.classList.add("hidden");
    companyForm.classList.remove("hidden");
}

function setFormBusy(isBusy, isUpdate) {
    saveButton.disabled = isBusy;
    cancelEditButton.disabled = isBusy;
    saveButton.textContent = isBusy
        ? isUpdate ? "Saving…" : "Submitting…"
        : isUpdate ? "Save application changes" : "Submit application";
}

function rememberApplication(id) {
    try {
        window.localStorage.setItem(LAST_APPLICATION_KEY, String(id));
    } catch {
        // The application still works when browser storage is unavailable.
    }

    resumeLastButton.dataset.applicationId = id;
    resumeLastButton.textContent = `Resume application #${id}`;
    resumeLastButton.classList.remove("hidden");
}

function restoreLastApplicationLink() {
    try {
        const id = Number(window.localStorage.getItem(LAST_APPLICATION_KEY));

        if (Number.isInteger(id) && id > 0) {
            resumeLastButton.dataset.applicationId = id;
            resumeLastButton.textContent = `Resume application #${id}`;
            resumeLastButton.classList.remove("hidden");
        }
    } catch {
        // Browser storage is optional.
    }
}

function formatCurrency(value) {
    return Number(value).toLocaleString("en-US", {
        style: "currency",
        currency: "USD",
        maximumFractionDigits: 0
    });
}

function formatStatus(status) {
    if (!status) {
        return "Submitted";
    }

    return status.charAt(0) + status.slice(1).toLowerCase();
}

async function readError(response) {
    try {
        const errorData = await response.json();
        const fieldError = errorData.fieldErrors
            ? Object.values(errorData.fieldErrors)[0]
            : null;
        const validationError = Array.isArray(errorData.errors)
            ? errorData.errors[0]?.defaultMessage
            : null;

        return fieldError
            || validationError
            || errorData.detail
            || errorData.message
            || errorData.error
            || `Request failed with status ${response.status}`;
    } catch {
        return `Request failed with status ${response.status}`;
    }
}

function populateCountryOptions() {
    const countrySelect = fields.countryOfRegistration;
    const displayNames = typeof Intl.DisplayNames === "function"
        ? new Intl.DisplayNames(["en"], { type: "region" })
        : null;

    const countries = ISO_COUNTRY_CODES.map(function (code) {
        return {
            code,
            name: displayNames?.of(code) || code
        };
    }).sort((first, second) => first.name.localeCompare(second.name));

    countries.forEach(function ({ code, name }) {
        const option = document.createElement("option");
        option.value = code;
        option.textContent = ONLINE_RESTRICTED_COUNTRIES.has(code)
            ? `${name} — online submission unavailable`
            : name;
        option.disabled = ONLINE_RESTRICTED_COUNTRIES.has(code);
        countrySelect.append(option);
    });

    countrySelect.value = "US";
}

function showStatus(message, type = "") {
    statusMessage.textContent = message;
    statusMessage.className = `status-message ${type}`;
}

function showLookupMessage(message, type = "") {
    lookupMessage.textContent = message;
    lookupMessage.className = `inline-message ${type}`;
}

function watchApplicationSections() {
    const sectionLinks = [...document.querySelectorAll(".progress-item")];
    const sections = sectionLinks
        .map((link) => getElement(link.dataset.section))
        .filter(Boolean);

    if (!("IntersectionObserver" in window)) {
        return;
    }

    const observer = new IntersectionObserver(function (entries) {
        const visibleSection = entries
            .filter((entry) => entry.isIntersecting)
            .sort((first, second) => second.intersectionRatio - first.intersectionRatio)[0];

        if (!visibleSection) {
            return;
        }

        sectionLinks.forEach(function (link) {
            link.classList.toggle("active", link.dataset.section === visibleSection.target.id);
        });
    }, {
        rootMargin: "-20% 0px -60%",
        threshold: [0.05, 0.25, 0.5]
    });

    sections.forEach((section) => observer.observe(section));
}

populateCountryOptions();
restoreLastApplicationLink();
watchApplicationSections();
