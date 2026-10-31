# Temp Number (Android)

White UI, har country ka flag, inbox me OTP card aur auto-refresh (har 5 second).
API key app ke andar build time par lagti hai, app me kahin show nahi hoti.

## GitHub se APK banane ke steps
1. GitHub par **private** repository banayein (key file me hai, public mat rakhein).
2. Is zip ki saari files repo me upload karein (`.github` folder samet).
3. Repo me **Actions** tab kholein, **Build APK** chalayein (ya bas push karein).
4. Build khatam hone par run ke neeche **Artifacts** se `TempNumber-APK` download karein, zip kholen, `app-debug.apk` phone me install karein.

## Settings badalni hon to
`secrets.properties` file edit karein (key, host, endpoint paths, parameter names), push karein, APK dobara ban jayegi.
Agar app me "Server error 404" ya "403" aaye to RapidAPI ke Code Snippets se host aur paths yahan theek karein.

Optional: key GitHub Secret `RAPIDAPI_KEY` me rakh dein, wo file ki key par priority leti hai.
