package com.tempnumber.app

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object C {
    val Green = Color(0xFF067A55)
    val GreenSoft = Color(0xFFE3F3EC)
    val Ink = Color(0xFF101828)
    val Muted = Color(0xFF5B6675)
    val Line = Color(0xFFE4E8EE)
    val Field = Color(0xFFF5F7FA)
}

@Composable
fun TempTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = C.Green,
            onPrimary = Color.White,
            background = Color.White,
            onBackground = C.Ink,
            surface = Color.White,
            onSurface = C.Ink,
            surfaceVariant = C.Field,
            outline = C.Line
        ),
        content = content
    )
}

@Composable
fun App(vm: AppViewModel) {
    BackHandler(enabled = vm.screen != Screen.Splash && vm.screen != Screen.Home) { vm.back() }
    Box(Modifier.fillMaxSize().background(Color.White)) {
        when (val s = vm.screen) {
            Screen.Splash -> SplashScreen(vm)
            Screen.Home, Screen.MyNumbers -> MainTabs(vm, s)
            is Screen.Numbers -> NumbersScreen(vm, s.country)
            is Screen.Inbox -> InboxScreen(vm, s)
        }
    }
}

// ---------- Shared pieces ----------

@Composable
fun Initials(name: String) {
    Text(name.take(2).uppercase(), color = C.Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun Flag(name: String, iso: String?, size: Dp = 40.dp) {
    Box(
        Modifier.size(size).clip(CircleShape).background(C.Field).border(1.dp, C.Line, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (iso != null) {
            SubcomposeAsyncImage(
                model = Flags.url(iso),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                loading = { Initials(name) },
                error = { Initials(name) }
            )
        } else {
            Initials(name)
        }
    }
}

@Composable
fun TopBar(
    title: String,
    onBack: () -> Unit,
    flag: Country? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = C.Ink)
        }
        if (flag != null) {
            Flag(flag.name, flag.iso, 32.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(
            title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = C.Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        actions()
    }
}

@Composable
fun Loading() {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = C.Green)
    }
}

@Composable
fun MessageBox(text: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text, color = C.Muted, fontSize = 15.sp, textAlign = TextAlign.Center, lineHeight = 22.sp)
        if (actionLabel != null) {
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(containerColor = C.Green, contentColor = Color.White),
                shape = RoundedCornerShape(14.dp)
            ) { Text(actionLabel, fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ---------- Splash ----------

@Composable
fun SplashScreen(vm: AppViewModel) {
    LaunchedEffect(Unit) {
        delay(1300)
        vm.go(Screen.Home)
    }
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(132.dp).clip(RoundedCornerShape(38.dp)).background(C.Green),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = "Temp Number logo",
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(24.dp))
        Text("Temp Number", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = C.Ink)
        Spacer(Modifier.height(8.dp))
        Text(
            "Get a temporary number and read your SMS codes in seconds.",
            fontSize = 16.sp,
            color = C.Muted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

// ---------- Tabs: Countries and My numbers ----------

@Composable
fun MainTabs(vm: AppViewModel, current: Screen) {
    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.White,
        selectedTextColor = C.Green,
        indicatorColor = C.Green,
        unselectedIconColor = C.Muted,
        unselectedTextColor = C.Muted
    )
    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = current == Screen.Home,
                    onClick = { vm.go(Screen.Home) },
                    icon = { Icon(Icons.Filled.Public, contentDescription = null) },
                    label = { Text("Countries") },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = current == Screen.MyNumbers,
                    onClick = { vm.go(Screen.MyNumbers) },
                    icon = { Icon(Icons.Filled.PhoneAndroid, contentDescription = null) },
                    label = { Text("My numbers") },
                    colors = itemColors
                )
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            if (current == Screen.Home) HomeScreen(vm) else MyNumbersScreen(vm)
        }
    }
}

@Composable
fun HomeScreen(vm: AppViewModel) {
    var query by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 14.dp)) {
            Text("Choose a country", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = C.Ink)
            Spacer(Modifier.height(4.dp))
            Text("Pick where your temporary number should come from.", fontSize = 14.sp, color = C.Muted)
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Search countries", color = C.Muted) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = C.Muted) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = C.Line,
                focusedBorderColor = C.Green,
                unfocusedContainerColor = C.Field,
                focusedContainerColor = C.Field,
                cursorColor = C.Green
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        when (val c = vm.countries) {
            Load.Loading -> Loading()
            is Load.Failure -> MessageBox(c.message, "Try again") { vm.loadCountries() }
            is Load.Done -> {
                val shown = c.data.filter { it.name.contains(query.trim(), ignoreCase = true) }
                if (shown.isEmpty()) {
                    MessageBox("No countries found.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(shown, key = { it.key }) { country ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, C.Line, RoundedCornerShape(16.dp))
                                    .clickable { vm.go(Screen.Numbers(country)) }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Flag(country.name, country.iso, 40.dp)
                                Spacer(Modifier.width(14.dp))
                                Text(
                                    country.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = C.Ink,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = C.Muted)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MyNumbersScreen(vm: AppViewModel) {
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 14.dp)) {
            Text("My numbers", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = C.Ink)
            Spacer(Modifier.height(4.dp))
            Text("Numbers you saved. Tap one to open its inbox.", fontSize = 14.sp, color = C.Muted)
        }
        if (vm.saved.isEmpty()) {
            MessageBox("You have not saved any number yet. Open an inbox and tap the star to keep it here.", "Get a number") {
                vm.go(Screen.Home)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vm.saved, key = { it.number }) { sv ->
                    val country = sv.country()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, C.Line, RoundedCornerShape(16.dp))
                            .clickable { vm.go(Screen.Inbox(country, sv.number, Screen.MyNumbers)) }
                            .padding(start = 14.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Flag(country.name, country.iso, 44.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(prettyNumber(sv.number), fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = C.Ink)
                            Text(country.name, fontSize = 13.sp, color = C.Muted)
                        }
                        IconButton(onClick = { vm.removeSaved(sv) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove ${prettyNumber(sv.number)}", tint = C.Muted)
                        }
                    }
                }
            }
        }
    }
}

// ---------- Number list ----------

@Composable
fun NumbersScreen(vm: AppViewModel, country: Country) {
    LaunchedEffect(country.key) { vm.loadNumbers(country) }
    Column(Modifier.fillMaxSize()) {
        TopBar(country.name, onBack = { vm.back() }, flag = country) {
            IconButton(onClick = { vm.loadNumbers(country) }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh numbers", tint = C.Ink)
            }
        }
        Text(
            "Pick a number to receive SMS",
            fontSize = 14.sp,
            color = C.Muted,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        when (val n = vm.numbers) {
            Load.Loading -> Loading()
            is Load.Failure -> MessageBox(n.message, "Try again") { vm.loadNumbers(country) }
            is Load.Done -> {
                if (n.data.isEmpty()) {
                    MessageBox("No numbers are available for this country right now. Try another country.")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(n.data, key = { it.number }) { p ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, C.Line, RoundedCornerShape(16.dp))
                                    .clickable { vm.go(Screen.Inbox(country, p.number, Screen.Numbers(country))) }
                                    .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(prettyNumber(p.number), fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = C.Ink)
                                    Spacer(Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(8.dp).clip(CircleShape).background(C.Green))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Active", fontSize = 13.sp, color = C.Muted)
                                    }
                                }
                                Box(
                                    Modifier.height(40.dp).clip(RoundedCornerShape(12.dp)).background(C.Green).padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Open inbox", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------- Inbox and OTP ----------

@Composable
fun InboxScreen(vm: AppViewModel, s: Screen.Inbox) {
    val clipboard = LocalClipboardManager.current
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(s.number) {
        vm.refreshMessages(s.number, silent = false)
        while (true) {
            delay(5000)
            vm.refreshMessages(s.number, silent = true)
        }
    }

    fun copy(text: String, toast: String) {
        clipboard.setText(AnnotatedString(text))
        Toast.makeText(ctx, toast, Toast.LENGTH_SHORT).show()
    }

    val isSaved = vm.isSaved(s.number)

    Column(Modifier.fillMaxSize()) {
        TopBar("Inbox", onBack = { vm.back() }, flag = s.country) {
            IconButton(onClick = { vm.toggleSaved(s.country, s.number) }) {
                Icon(
                    if (isSaved) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = if (isSaved) "Remove from my numbers" else "Save number",
                    tint = if (isSaved) C.Green else C.Ink
                )
            }
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(C.Field)
                        .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Your number · ${s.country.name}", fontSize = 13.sp, color = C.Muted)
                        Spacer(Modifier.height(4.dp))
                        Text(prettyNumber(s.number), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = C.Ink)
                    }
                    IconButton(onClick = { copy(prettyNumber(s.number), "Number copied") }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "Copy number", tint = C.Ink)
                    }
                }
            }
            val m = vm.messages
            when (m) {
                Load.Loading -> item { Loading() }
                is Load.Failure -> item { MessageBox(m.message, "Try again") { scope.launch { vm.refreshMessages(s.number, silent = false) } } }
                is Load.Done -> {
                    val latest = m.data.firstNotNullOfOrNull { sms -> extractCode(sms.text)?.let { code -> sms to code } }
                    item {
                        if (latest != null) {
                            Column(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.Green).padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        if (latest.first.sender.isNotEmpty()) "Latest code · ${latest.first.sender}" else "Latest code",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (latest.first.time.isNotEmpty()) {
                                        Text(latest.first.time, color = Color.White, fontSize = 13.sp)
                                    }
                                }
                                Text(groupCode(latest.second), color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = { copy(latest.second, "Code copied") },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = C.Green)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Copy code", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Column(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(C.GreenSoft).padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Waiting for your code", color = C.Green, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "Use this number on the website or app, then stay here. New SMS appear automatically.",
                                    color = C.Ink,
                                    fontSize = 14.sp,
                                    lineHeight = 21.sp
                                )
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("All messages", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = C.Ink, modifier = Modifier.weight(1f))
                            Box(Modifier.size(8.dp).clip(CircleShape).background(C.Green))
                            Spacer(Modifier.width(6.dp))
                            Text("Checking every 5 s", fontSize = 13.sp, color = C.Muted)
                        }
                    }
                    if (m.data.isEmpty()) {
                        item { MessageBox("No messages yet.") }
                    } else {
                        items(m.data) { sms ->
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, C.Line, RoundedCornerShape(14.dp))
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        sms.sender.ifEmpty { "SMS" },
                                        color = C.Green,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (sms.time.isNotEmpty()) Text(sms.time, color = C.Muted, fontSize = 13.sp)
                                }
                                Text(sms.text, color = C.Ink, fontSize = 15.sp, lineHeight = 21.sp)
                            }
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)) {
            OutlinedButton(
                onClick = { scope.launch { vm.refreshMessages(s.number, silent = true) } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, C.Line),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = C.Ink)
            ) {
                if (vm.refreshing) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = C.Green, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                Text(if (vm.refreshing) "Refreshing..." else "Refresh messages", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
