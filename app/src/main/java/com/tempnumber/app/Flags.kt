package com.tempnumber.app

object Flags {
    private val byName = mapOf(
        "united states" to "us", "usa" to "us", "united states of america" to "us", "america" to "us",
        "united kingdom" to "gb", "uk" to "gb", "great britain" to "gb", "england" to "gb",
        "canada" to "ca", "germany" to "de", "france" to "fr", "india" to "in", "pakistan" to "pk",
        "netherlands" to "nl", "holland" to "nl", "sweden" to "se", "norway" to "no", "denmark" to "dk",
        "finland" to "fi", "poland" to "pl", "spain" to "es", "portugal" to "pt", "italy" to "it",
        "switzerland" to "ch", "austria" to "at", "belgium" to "be", "ireland" to "ie", "czech republic" to "cz",
        "czechia" to "cz", "romania" to "ro", "bulgaria" to "bg", "hungary" to "hu", "greece" to "gr",
        "ukraine" to "ua", "russia" to "ru", "turkey" to "tr", "turkiye" to "tr", "israel" to "il",
        "saudi arabia" to "sa", "united arab emirates" to "ae", "uae" to "ae", "egypt" to "eg",
        "south africa" to "za", "nigeria" to "ng", "kenya" to "ke", "morocco" to "ma", "ghana" to "gh",
        "china" to "cn", "hong kong" to "hk", "japan" to "jp", "south korea" to "kr", "korea" to "kr",
        "taiwan" to "tw", "thailand" to "th", "vietnam" to "vn", "indonesia" to "id", "malaysia" to "my",
        "singapore" to "sg", "philippines" to "ph", "bangladesh" to "bd", "sri lanka" to "lk", "nepal" to "np",
        "australia" to "au", "new zealand" to "nz", "brazil" to "br", "argentina" to "ar", "chile" to "cl",
        "colombia" to "co", "peru" to "pe", "mexico" to "mx", "venezuela" to "ve", "estonia" to "ee",
        "latvia" to "lv", "lithuania" to "lt", "croatia" to "hr", "serbia" to "rs", "slovakia" to "sk",
        "slovenia" to "si", "iceland" to "is", "luxembourg" to "lu", "kazakhstan" to "kz", "iran" to "ir",
        "iraq" to "iq", "jordan" to "jo", "lebanon" to "lb", "qatar" to "qa", "kuwait" to "kw",
        "oman" to "om", "bahrain" to "bh", "cambodia" to "kh", "myanmar" to "mm", "laos" to "la"
    )

    /** Two-letter ISO code for a flag image, from the API code if it looks like one, else from the name. */
    fun isoFor(name: String, code: String?): String? {
        val c = code?.trim()?.lowercase()
        if (c != null && c.length == 2 && c.all { it in 'a'..'z' }) {
            return if (c == "uk") "gb" else c
        }
        return byName[name.trim().lowercase()]
    }

    fun url(iso: String) = "https://flagcdn.com/w160/$iso.png"
}
