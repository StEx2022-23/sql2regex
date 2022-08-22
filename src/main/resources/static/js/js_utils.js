let scrolltotopbutton = document.getElementById("scrolltotop");

window.onscroll = function () {
    scrollFunction()
};

function scrollFunction() {
    if (document.body.scrollTop > 15 || document.documentElement.scrollTop > 15) {
        scrolltotopbutton.style.display = "block";
    } else {
        scrolltotopbutton.style.display = "none";
    }
}

function topFunction() {
    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;
}

function handleAlerts(id){
    if(document.getElementById(id) !== null && !document.getElementById(id).classList.contains("show")){
        new bootstrap.Toast(document.getElementById(id)).show()
    }
}

function copy2clipbord(id, showAlert) {
    let copyText = document.getElementById(id);

    navigator.clipboard.writeText(copyText.value).then(function() {
        if(showAlert === true) new bootstrap.Toast(document.getElementById("alert-success-copy")).show()
    }, function() {
        if(showAlert === true)
            new bootstrap.Toast(document.getElementById("alert-warning-copy")).show()
    }).catch(function () {
        let copyElement = document.createElement("input");
        copyElement.setAttribute("value", copyText.value);
        document.body.appendChild(copyElement);
        copyElement.focus();
        copyElement.select();
        document.execCommand("copy");
        document.body.removeChild(copyElement);
    });
}

function copyHistoryEntry(id) {
    navigator.clipboard.writeText(document.getElementById(id).innerHTML).then(_r => console.log("Successfully copied! :-)"));
}

function insertVisualizationPage() {
    let src = "https://regexper.com/#";
    let params = new URLSearchParams(document.location.search);
    if(null !== params.get("regex") && params.get("regex").length !== 0){
        src = src + params.get("regex");
    } else {
        src = src + encodeURIComponent("SELECT \\* FROM table");
    }

    let iframe = document.createElement('iframe');
    iframe.setAttribute("width", "100%");
    iframe.setAttribute("height", "800");
    iframe.setAttribute("scrollbar", "scrollbar");
    iframe.setAttribute("id", "visualizationOption1");
    iframe.setAttribute("src", src);

    let container = document.getElementById("visualizationOption1ResourceHint");
    container.appendChild(iframe);
}

function moveToVisualizationPage(regexoutput){
    window.location.href="/visualization?"+window.location.href.split('?')[1] +"&regex="+encodeURIComponent(document.getElementById(regexoutput).innerHTML)
}

function handleVisualization(regexoutput) {
    let MAX_URL_LENGTH = 2048;
    let parsedUrl = "/visualization?" + window.location.href.split("?")[1] + "&regex=" + encodeURIComponent(document.getElementById(regexoutput).innerHTML);
    const urlTooLongToast = new bootstrap.Toast(document.getElementById("toast-too-long-url-for-visualization"))
    if(parsedUrl.length > MAX_URL_LENGTH) {
        urlTooLongToast.show();
        return;
    }
    moveToVisualizationPage(regexoutput);
}

function formattedCurrentTimestamp() {
    let currentdate = new Date();
    return ((currentdate.getDate().toString().length === 1) ? "0".concat(currentdate.getDate()) : (currentdate.getDate())) + "/" + (((currentdate.getMonth() + 1).toString().length === 1) ? "0".concat(currentdate.getMonth() + 1) : (currentdate.getMonth() + 1)) + "/" + ((currentdate.getFullYear().toString().length === 1) ? "0".concat(currentdate.getFullYear()) : (currentdate.getFullYear())) + " @ " + ((currentdate.getHours().toString().length === 1) ? "0".concat(currentdate.getHours()) : (currentdate.getHours())) + ":" + ((currentdate.getMinutes().toString().length === 1) ? "0".concat(currentdate.getMinutes()) : (currentdate.getMinutes())) + ":" + ((currentdate.getSeconds().toString().length === 1) ? "0".concat(currentdate.getSeconds()) : (currentdate.getSeconds()));
}

function generateJsonFormatFile(id_sql, id_regex) {
    let element = document.createElement('a');
    let sql = document.getElementById(id_sql).value;
    let regex = document.getElementById(id_regex).value;

    let jsonformat = '{"sql":"' + sql + '", "regex":"' + regex + '", "website":"sql2regex.herokuapp.com", "timestamp":"' + formattedCurrentTimestamp() + '"}'
    let filename = "sql2regex_" + formattedCurrentTimestamp().replaceAll(" @ ", "").replaceAll("/", "").replaceAll(":", "") + ".json";

    try{
        element.setAttribute("href", "data:text/plain;charset=utf-8," + encodeURIComponent(jsonformat));
        element.setAttribute("download", filename);
        element.style.display = "none";
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
    } catch (e) {
        console.log(e);
        handleAlerts("json-warning-export");
        return;
    }
    handleAlerts("json-success-export");
}

const animateRegExCheck = [
    {transform: 'scale(0.98)'},
    {transform: 'scale(1.02)'},
    {transform: 'scale(1.0)'}
];

const animateRegExCheckTiming = {
    duration: 500,
    iterations: 1,
}

function checkRegExExample(sql, regex) {
    let text = document.getElementById(sql).value;
    let parsedRegEx = document.getElementById(regex).innerHTML;
    parsedRegEx = parsedRegEx.substring(parsedRegEx.indexOf("^"), parsedRegEx.length);
    parsedRegEx = parsedRegEx.substring(0, parsedRegEx.indexOf("$") + 1);
    parsedRegEx = parsedRegEx.replaceAll("&amp;", "&");
    let nullInputHint = document.getElementById(regex + "hint");
    let pattern = new RegExp(parsedRegEx, 'gmi');
    let checkPositive = document.getElementById(regex + "pos");
    let checkNegative = document.getElementById(regex + "neg");

    checkPositive.style.display = "none";
    checkNegative.style.display = "none";

    if (text.length === 0) {
        nullInputHint.style.display = "block";
        nullInputHint.animate(animateRegExCheck, animateRegExCheckTiming);
        return;
    } else {
        nullInputHint.style.display = "none";
    }

    if (pattern.test(text)) {
        checkPositive.style.display = "block";
        checkPositive.animate(animateRegExCheck, animateRegExCheckTiming);
    } else {
        checkNegative.style.display = "block";
        checkNegative.animate(animateRegExCheck, animateRegExCheckTiming);
    }
}

function onChangePresetSelect(textinputfieldid, selectfieldid) {
    let textinputfield = document.getElementById(textinputfieldid);
    let selectfield = document.getElementById(selectfieldid).options;
    textinputfield.value = selectfield[selectfield.selectedIndex].value;
}

class SqlRegExHistory {
    constructor(localStorageId) {
        let ls = localStorage.getItem(localStorageId);
        this.localStorageId = localStorageId;
        if (ls != null) {
            this.sql = JSON.parse(ls)[0];
            this.regex = JSON.parse(ls)[1];
        } else {
            this.sql = [];
            this.regex = [];
            this.writeToLocalStorage(this);
        }
    }

    writeToLocalStorage() {
        let toWrittenArrayOfArray = [this.sql, this.regex];
        localStorage.setItem(this.localStorageId, JSON.stringify(toWrittenArrayOfArray));
    }

    readSqlFromLocalStorage() {
        if (localStorage.getItem(this.localStorageId) != null) {
            let ls = localStorage.getItem(this.localStorageId);
            return JSON.parse(ls)[0];
        } else return -1;
    }

    readRegExFromLocalStorage() {
        if (localStorage.getItem(this.localStorageId) != null) {
            let ls = localStorage.getItem(this.localStorageId);
            return JSON.parse(ls)[1];
        } else return -1;
    }

    readSqlRegExFromLocalStorage() {
        return [this.readSqlFromLocalStorage(), this.readRegExFromLocalStorage()];
    }

    addToLocalStorage(sqlNew, regexNew) {
        this.sql.push(sqlNew);
        this.regex.push(regexNew);
        this.writeToLocalStorage();
    }

    checkUpdatedConverting() {
        if(null !== document.getElementById("convertbodycontainer")) {
            let sqlinput = document.getElementById("sqlinput").value;
            let regexinput = document.getElementById("regexoutput").value;
            if (!this.sql.includes(regexinput) && regexinput.length !== 0 && sqlinput.length !== 0) this.addToLocalStorage(sqlinput, regexinput);
            if (this.regex.length !== 0) this.showConvertingHistory();
        }
    }

    showConvertingHistory() {
        let arrayOfSqlAndArrayOfRegex = this.readSqlRegExFromLocalStorage();
        let body = document.getElementById("table-container");

        while (body.firstChild) {
            body.removeChild(body.lastChild);
        }

        let outerDiv = document.createElement('div');
        outerDiv.classList.add("accordion","mb-3");
        outerDiv.setAttribute("id","historyAccordion")

        if (arrayOfSqlAndArrayOfRegex[0] !== -1) {
            for (let i = 0; i < this.sql.length; i++) {
                let tempAccordionDiv = document.createElement('div');
                tempAccordionDiv.classList.add("accordion-item");
                tempAccordionDiv.setAttribute("id", "#"+String(i));

                let topHeading = document.createElement('div');
                topHeading.classList.add("accordion-header");
                topHeading.setAttribute("id", "heading-"+String(i));

                let button = document.createElement('button');
                button.classList.add("accordion-button", "collapsed");
                button.setAttribute("type", "button");
                button.setAttribute("data-bs-toggle", "collapse");
                button.setAttribute("data-bs-target", "#collapse-"+String(i));
                button.setAttribute("aria-expanded", "false");
                button.setAttribute("aria-controls", "collapse-"+String(i));
                button.innerHTML = arrayOfSqlAndArrayOfRegex[0][i];
                topHeading.append(button);

                let alreadyFound = false;
                for(let j = 0; j < i; j++){
                    // check if sql is equal
                    if(arrayOfSqlAndArrayOfRegex[0][i] === arrayOfSqlAndArrayOfRegex[0][j]){
                        // check if regex is equal
                        if(arrayOfSqlAndArrayOfRegex[1][i] === arrayOfSqlAndArrayOfRegex[1][j]) alreadyFound = true;
                        else {
                            let existingAccordionBodys = [];
                            outerDiv.childNodes.forEach(el => {existingAccordionBodys.push(el.childNodes[1].childNodes[0])});
                            existingAccordionBodys.forEach(el => {
                                if(el.id === "accordionBodyId-"+String(j)){
                                    let divider = document.createElement("hr");
                                    el.append(divider);
                                    el.append(this.generateCopyIconAndCodeContainer(i));
                                    alreadyFound = true;
                                }
                            });
                         }
                    }
                }

                if(!alreadyFound){
                    let innerInnerDiv = document.createElement('div');
                    innerInnerDiv.setAttribute("id", "collapse-"+String(i));
                    innerInnerDiv.setAttribute("aria-labelledby", "heading-"+String(i));
                    innerInnerDiv.setAttribute("data-bs-parent", "#historyAccordion");
                    innerInnerDiv.classList.add("accordion-collapse","collapse");

                    let innerInnerInnerDiv = document.createElement('div');
                    innerInnerInnerDiv.classList.add("accordion-body");
                    innerInnerInnerDiv.setAttribute("id", "accordionBodyId-"+String(i))

                    innerInnerInnerDiv.append(this.generateCopyIconAndCodeContainer(i))
                    innerInnerDiv.append(innerInnerInnerDiv);

                    tempAccordionDiv.append(topHeading);
                    tempAccordionDiv.append(innerInnerDiv);
                    outerDiv.append(tempAccordionDiv);
                }
            }
        }
        body.appendChild(outerDiv);
        
        if (!document.getElementById("convertbodycontainer").classList.contains("show")) {
            new bootstrap.Collapse(document.getElementById("convertbodycontainer"))
        }
    }

    generateCopyIconAndCodeContainer(i) {
        let copyIconHolder = document.createElement('div');
        copyIconHolder.setAttribute("id", "copyIconHolderFor-"+String(i));
        copyIconHolder.style.position = "relative";

        let codeTag = document.createElement("code");
        codeTag.setAttribute("id", "copyHistoryCodeId-"+String(i))
        codeTag.innerHTML = this.readSqlRegExFromLocalStorage()[1][i];

        let copyDiv = document.createElement("div");
        copyDiv.setAttribute("id","copyHistory");
        let copyEntryId = "copyHistoryCodeId-".concat(String(i));
        copyDiv.setAttribute("onclick", "copyHistoryEntry('"+copyEntryId+"')");
        copyDiv.append(this.generateCopyIcon());

        copyIconHolder.append(codeTag);
        copyIconHolder.append(copyDiv);

        return copyIconHolder;
    }
    generateCopyIcon() {
        let copyIcon = document.createElementNS('http://www.w3.org/2000/svg', "svg");
        copyIcon.setAttribute("width", "30");
        copyIcon.setAttribute("height", "30");
        copyIcon.setAttribute("fill", "black");
        copyIcon.setAttribute("viewBox", "0 0 16 16");
        copyIcon.setAttribute("class", "icon");
        let copyIconPathEins = document.createElementNS('http://www.w3.org/2000/svg', "path");
        copyIconPathEins.setAttribute("d", "M9.5 0a.5.5 0 0 1 .5.5.5.5 0 0 0 .5.5.5.5 0 0 1 .5.5V2a.5.5 0 0 1-.5.5h-5A.5.5 0 0 1 5 2v-.5a.5.5 0 0 1 .5-.5.5.5 0 0 0 .5-.5.5.5 0 0 1 .5-.5h3Z");
        copyIcon.append(copyIconPathEins);
        let copyIconPathZwei = document.createElementNS('http://www.w3.org/2000/svg', "path");
        copyIconPathZwei.setAttribute("d", "M3 2.5a.5.5 0 0 1 .5-.5H4a.5.5 0 0 0 0-1h-.5A1.5 1.5 0 0 0 2 2.5v12A1.5 1.5 0 0 0 3.5 16h9a1.5 1.5 0 0 0 1.5-1.5v-12A1.5 1.5 0 0 0 12.5 1H12a.5.5 0 0 0 0 1h.5a.5.5 0 0 1 .5.5v12a.5.5 0 0 1-.5.5h-9a.5.5 0 0 1-.5-.5v-12Z");
        copyIcon.append(copyIconPathZwei);
        let copyIconPathDrei = document.createElementNS('http://www.w3.org/2000/svg', "path");
        copyIconPathDrei.setAttribute("d", "M10.854 7.854a.5.5 0 0 0-.708-.708L7.5 9.793 6.354 8.646a.5.5 0 1 0-.708.708l1.5 1.5a.5.5 0 0 0 .708 0l3-3Z");
        copyIcon.append(copyIconPathDrei);
        return copyIcon;
    }

    clearLocalStorage() {
        try{
            this.sql = [];
            this.regex = [];
            this.writeToLocalStorage(this);
            new bootstrap.Toast(document.getElementById("clear-local-storage-success"))
        } catch (e) {
            console.log(e);
            handleAlerts("clear-local-storage-warning");
        }
        new bootstrap.Collapse(document.getElementById("convertbodycontainer"));
    }

    downloadJsonOfHistory() {
        try{
            let converts = {}
            let ConvertingHistory = {}
            for (let i = 0; i < this.readSqlRegExFromLocalStorage()[0].length; i++) {
                let SingleConvert = {};
                SingleConvert["sql"] = this.readSqlRegExFromLocalStorage()[0][i];
                SingleConvert["regex"] = this.readSqlRegExFromLocalStorage()[1][i];
                converts[i] = SingleConvert;
            }
            ConvertingHistory["results"] = converts;
            ConvertingHistory["website"] = "sql2regex.herokuapp.com";
            ConvertingHistory["timestamp"] = formattedCurrentTimestamp();

            let filename = "sql2regex_convertinghistory_" + formattedCurrentTimestamp().replaceAll(" @ ", "").replaceAll("/", "").replaceAll(":", "") + ".json";
            let element = document.createElement('a');
            element.setAttribute("href", "data:text/plain;charset=utf-8," + encodeURIComponent(JSON.stringify(ConvertingHistory, null, "\t")));
            element.setAttribute("download", filename);
            element.style.display = "none";
            document.body.appendChild(element);
            element.click();
            document.body.removeChild(element);
            handleAlerts("json-success-export");
        } catch (e) {
            console.log(e);
            handleAlerts("json-warning-export");
        }

    }
}

function toggleSlaveCheckboxes(master, slaveName){
    const slaveArray = document.getElementsByName(slaveName);
    slaveArray.forEach((el) => {
        document.getElementById(el.id).checked = master.checked;
        updateSingleUserSetting(el);
    })
}

function languageChange(){
    let langOption = window.location.href.split("?")[1];
    if(langOption.length !== 0){
        if(langOption === "lang=de"){
            localStorage.setItem("defaultLanguage", "en_us");
            window.location.replace(window.location.href.split("?")[0] + '?lang=en_us');
        } else {
            localStorage.setItem("defaultLanguage", "de");
            window.location.replace(window.location.href.split("?")[0] + '?lang=de');
        }
    } else {
        window.location.replace(window.location.href.split("?")[0] + '?lang=en_us');
    }
}

function loadDefaultLanguageSettings(){
    let defaultSettings = localStorage.getItem("defaultLanguage");
    let navItems = document.querySelectorAll("#navbarlinks > a.nav-item.nav-link.fw-bolder");
    navItems.forEach((el) => {
       el.href = el.href + '?lang=' + defaultSettings
    });

    if(window.location.href.includes("lang")) return;
    if(defaultSettings !== null){
        window.location.replace(window.location.href.split("?")[0] + '?lang=' + defaultSettings);
    } else {
        window.location.replace(window.location.href.split("?")[0] + '?lang=en_us');
    }
}

function loadUserFormSettings(formElement){
    let allCheckboxes = formElement.querySelectorAll("input[type='checkbox']");
    if(localStorage.getItem("savedUserSettings") !== null){
        const settingsToast = new bootstrap.Toast(document.getElementById("toast-loaded-user-settings"))
        settingsToast.show();
        let savings = JSON.parse(localStorage.getItem("savedUserSettings"));
        allCheckboxes.forEach(checkbox => {
            if(undefined !== savings[checkbox.id]) {
                checkbox.checked = savings[checkbox.id];
            }
            if (checkbox.id.includes("master")){
                setCheckboxState(checkbox, slaveSelectionState(savings, checkbox.id.split("_")[0]))
            }
        })
    } else {
        let settingsDict = {};
        allCheckboxes.forEach(el => {
            settingsDict[el.id] = el.checked;
        })
        localStorage.setItem("savedUserSettings", JSON.stringify(settingsDict));
    }
}

function updateSingleUserSetting(inputElement){
    let settingsDict = {}
    if(localStorage.getItem("savedUserSettings") !== null){
        settingsDict = JSON.parse(localStorage.getItem("savedUserSettings"));
    }

    if(settingsDict[inputElement.id] !== undefined){
        if(settingsDict[inputElement.id] !== inputElement.checked){
            settingsDict[inputElement.id] = inputElement.checked;
        }
    } else {
        settingsDict[inputElement.id] = inputElement.checked;
    }

    const settingsOption = inputElement.id.split("_")[0]
    setCheckboxState(document.getElementById(settingsOption + "_master"), slaveSelectionState(settingsDict, settingsOption))

    localStorage.setItem("savedUserSettings", JSON.stringify(settingsDict));
}

function setCheckboxState(checkbox, state){
    if (state === 0){
        checkbox.indeterminate = false
        checkbox.checked = false
    } else if (state === 1){
        checkbox.indeterminate = true
    } else if (state === 2){
        checkbox.indeterminate = false
        checkbox.checked = true
    }
}

function slaveSelectionState(checkboxDict, groupName){
    const elOfNameOfEvent = Object.entries(checkboxDict).filter(([k,_v]) => k.includes(groupName) && !k.includes("master"))
    const elListOfActivated = elOfNameOfEvent.filter(([k,v]) => v === true && !k.includes("master"))
    if (elOfNameOfEvent.length === elListOfActivated.length){
        //all elements are activated
        return 2
    }else if (elListOfActivated.length === 0){
        //only the master checkbox is activated
        return 0
        //some slave checkboxes are activated, but not all
    }else{
        return 1
    }
}

function resetUserSettings(formElement){
    localStorage.removeItem("savedUserSettings");
    let allCheckboxes = formElement.querySelectorAll('input[type="checkbox"]');
    allCheckboxes.forEach(checkbox => {
        document.getElementById(checkbox.id).checked = true;
    })
}

function setCookiesAccepted(){
    localStorage.setItem("cookiesAccepted", "true");
}

function isCookiesAccepted(){
    return localStorage.getItem("cookiesAccepted") !== null;
}

function handleCookieBanner(){
    let cookieBanner = new bootstrap.Modal(document.getElementById('cockieBanner'), {
        keyboard: false,
        backdrop: 'static'
    });
    if(!isCookiesAccepted()) cookieBanner.show();
    loadDefaultLanguageSettings();
}

function scrollToInValidInput(el) {
    if(el !== null){
        let bodyRect = document.body.getBoundingClientRect()
        let elemRect = document.getElementById("isInValid").parentNode.getBoundingClientRect()
        let offset = (elemRect.top - bodyRect.top) * 0.9;

        window.scroll({
            top: offset,
            behavior: "smooth"
        });
    }
}

String.prototype.replaceAt = function(index, toReplace, replacement) {
    return this.substring(0, index) + replacement + this.substring(index + toReplace.length);
}

function handleDateValue(sqlInputEl) {
    let dateValue = sqlInputEl.value;
    const matches = [];
    let match = null;

    let regexDate = new RegExp(/\d\d\d\d-\d\d-\d\d/g);
    while ((match = regexDate.exec(dateValue)) != null){
        if (!(dateValue?.[match.index - 1] === "'" || dateValue?.[match.index - 2] === "d" || dateValue?.[match.index - 3] === "{")){
            matches.push({
                start: match.index,
                end: regexDate.lastIndex,
                value: match[0]
            })
        }
    }

    let regexDateTime = new RegExp(/\d\d\d\d-\d\d-\d\d \d\d:\d\d(?::\d\d)?/g);
    while ((match = regexDateTime.exec(dateValue)) != null){
        if (!(dateValue?.[match.index - 1] === "'" || dateValue?.[match.index - 2] === "s" || dateValue?.[match.index - 3] === "t" || dateValue?.[match.index - 4] === "{")){
            matches.push({
                start: match.index,
                end: regexDateTime.lastIndex,
                value: match[0]
            })
        }
    }

    let regexTime = new RegExp(/\d\d:\d\d(?::\d\d)?/g);
    while ((match = regexTime.exec(dateValue)) != null){
        if (!(dateValue?.[match.index - 1] === "'" || dateValue?.[match.index - 2] === "t" || dateValue?.[match.index - 3] === "{")){
            matches.push({
                start: match.index,
                end: regexTime.lastIndex,
                value: match[0]
            })
        }
    }

    const selectEl = document.getElementById("dateAndTimeSelect")

    selectEl.innerHTML = "";


    matches.forEach(singleMatch => {
        selectEl.appendChild(renderOption(singleMatch))
    })

    if(matches.length !== 0) {
        selectEl.setAttribute("size", matches.length)
        showDateValueHint()
    }
}

function renderOption(information){
    let element = document.createElement("option")
    element.setAttribute("value", information.start)
    element.innerHTML = information.value

    return element
}

function performDateAndTimeConversion(sqlInputEl, selectEl){
    const options = selectEl && selectEl.options
    let opt;
    for (let i = 0, iLen = options.length; i < iLen; i++) {
        opt = options[i];

        if (!opt.selected && opt.value !== 'NONE') {
            let endSubstring = parseInt(opt.value) + parseInt(opt.text.length);
            let afterDateTime = sqlInputEl.value.substring(endSubstring);

            sqlInputEl.value = sqlInputEl.value.replaceAt(
                opt.value,
                opt.text,
                dateOrTimeStringToLiteral(
                    sqlInputEl.value.substring(
                        opt.value,
                        endSubstring
                    )
                )
            )

            sqlInputEl.value = sqlInputEl.value + afterDateTime;
        }
        opt.parentElement.removeChild(opt)
    }

    selectEl.setAttribute("size", 1)
}

function dateOrTimeStringToLiteral(dateString) {
    let regexDate = new RegExp(/\d\d\d\d-\d\d-\d\d/gi);
    let regexDateTime = new RegExp(/(?=\d\d\d\d-\d\d-\d\d \d\d:\d\d(?::\d\d)?)/gi);
    let regexTime = new RegExp(/(?=\d\d:\d\d(?::\d\d)?)/gi);

    if(dateString.match(regexDate)?.length >= 1){
        return dateString.replace(dateString, "{d'$&'}");
    } else if (dateString.match(regexDateTime)?.length >= 1) {
        return dateString.replace(dateString, "{ts'$&'}");
    } else if (dateString.match(regexTime)?.length >= 1) {
        return dateString.replace(dateString, "{t'$&'}");
    }
    return "";
}

function showDateValueHint(){
    if(!document.getElementById("toast-date-and-time-value").classList.contains("show")){
        const dateAndTimeValueToast = new bootstrap.Toast(document.getElementById("toast-date-and-time-value"))
        dateAndTimeValueToast.show();
    }
}

let SqlRegExHis = new SqlRegExHistory("SqlRegExHistory");

document.onreadystatechange = function () {
    if (document.readyState === "interactive") {
        handleCookieBanner();
        handleAlerts();
        let currentDomain = window.location.href.split("/");
        let actualPath = currentDomain[currentDomain.length - 1].split("?")[0];

        if(actualPath === ""){
            document.addEventListener("submit",  (e) => {
                const sqlInputEl = document.getElementById("sqlinput")
                const selectEl = document.getElementById("dateAndTimeSelect")
                cacheInput = sqlInputEl.value;
                performDateAndTimeConversion(sqlInputEl, selectEl)
                const form = e.target;
                form.parentNode.parentNode.style.minHeight = form.clientHeight
                fetch(form.action, {
                    method: form.method,
                    body: new FormData(form),
                })
                    .then((res) => res.text())
                    .then((text) => new DOMParser().parseFromString(text, "text/html"))
                    .then((doc) => {
                        const result = document.createElement("div");
                        result.innerHTML = doc.body.innerHTML;
                        form.parentNode.parentNode.replaceChild(result, form.parentNode);
                        result.focus();
                    })
                    .then(() => SqlRegExHis.checkUpdatedConverting())
                    .then(() => copy2clipbord("regexoutput", false))
                    .then(() => scrollToInValidInput(document.getElementById("isInValid")))
                    .then(() => document.getElementById("sqlinput").value = cacheInput)
                    .then(() => handleDateValue(sqlInputEl));
                e.preventDefault();
                form.parentNode.parentNode.style.removeProperty("minHeight");
            })
            SqlRegExHis.checkUpdatedConverting();
            loadUserFormSettings(document.getElementById("converterForm"));
        } else if(actualPath === "visualization"){
            insertVisualizationPage();
        }
    }
}