package components

//import androidx.compose.ui.text.font.FontStyle
//import androidx.compose.ui.text.font.FontWeight
import LocalCtrl
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import dialog.SettingsDialog
import kotlinx.coroutines.launch
import player.isMacOS
import player.isWindows
import state.AppState
import state.TypingType
import theme.createColors

/**
 * 侧边菜单
 */
@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
    kotlinx.serialization.ExperimentalSerializationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class
)
@Composable
fun TypingWordSidebar(state: AppState) {

    if (state.openSettings) {
        val scope = rememberCoroutineScope()
        Box(Modifier.width(216.dp).fillMaxHeight()){
            val stateVertical = rememberScrollState(0)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(stateVertical)
            ) {
                Spacer(Modifier.fillMaxWidth().height(if (isMacOS()) 78.dp else 48.dp))
                Divider()
                val ctrl = LocalCtrl.current
                val tint = if (MaterialTheme.colors.isLight) Color.DarkGray else MaterialTheme.colors.onBackground
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            state.global.type = TypingType.SUBTITLES
                            state.saveGlobalState()
                        }

                    }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("抄写字幕", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+U",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Icon(
                        Icons.Filled.Subtitles,
                        contentDescription = "Localized description",
                        tint = tint,
                        modifier = Modifier.size(48.dp, 48.dp).padding(top = 12.dp, bottom = 12.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable {
                        scope.launch {
                            state.global.type = TypingType.TEXT
                            state.saveGlobalState()
                        }

                    }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("抄写文本", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+T",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Icon(
                        Icons.Filled.Title,
                        contentDescription = "Localized description",
                        tint = tint,
                        modifier = Modifier.size(48.dp, 48.dp).padding(top = 12.dp, bottom = 12.dp)
                    )
                }
                Divider()
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("显示单词", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+V",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.wordVisible,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.wordVisible = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }
                        },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text(text = "显示音标", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+P",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.phoneticVisible,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.phoneticVisible = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }
                        },

                        )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("显示词形", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+L",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.morphologyVisible,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.morphologyVisible = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }

                        },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("英文释义", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+E",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.definitionVisible,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.definitionVisible = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }
                        },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("中文释义", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+K",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.translationVisible,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.translationVisible = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }

                        },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("显示字幕", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+S",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.subtitlesVisible,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.subtitlesVisible = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }

                        },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("显示速度", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+N",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.speedVisible,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.speedVisible = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }
                        },
                    )
                }
                Divider()
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text(text = "自动切换", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+A",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.isAuto,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.isAuto = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }

                        },

                        )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("深色模式", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+D",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.global.isDarkTheme,
                        onCheckedChange = {
                            scope.launch {
                                state.global.isDarkTheme = it
                                state.colors = createColors(state.global.isDarkTheme, state.global.primaryColor)
                                state.saveGlobalState()
                            }

                        },
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("击键音效", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+M",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.global.isPlayKeystrokeSound,
                        onCheckedChange = {
                            scope.launch {
                                state.global.isPlayKeystrokeSound = it
                                state.saveGlobalState()
                            }
                        },

                        )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                        .clickable { }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("提示音效", color = MaterialTheme.colors.onBackground)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$ctrl+Q",
                            color = MaterialTheme.colors.onBackground
                        )
                    }

                    Spacer(Modifier.width(15.dp))
                    Switch(
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colors.primary),
                        checked = state.typingWord.isPlaySoundTips,
                        onCheckedChange = {
                            scope.launch {
                                state.typingWord.isPlaySoundTips = it
                                if (!state.isDictation) {
                                    state.saveTypingWordState()
                                }
                            }
                        },

                        )
                }
                Divider()
                var expanded by remember { mutableStateOf(false) }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                        .clickable {  expanded = true }.padding(start = 16.dp, end = 8.dp)
                ) {
                    Row {
                        Text("音量控制", color = MaterialTheme.colors.onBackground)
                    }
                    Spacer(Modifier.width(15.dp))
                    CursorDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        Surface(
                            elevation = 4.dp,
                            shape = RectangleShape,
                        ) {
                            Column(Modifier.width(300.dp).height(180.dp).padding(start = 16.dp, end = 16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("击键音效")
                                    Slider(value = state.global.keystrokeVolume, onValueChange = {
                                        Thread(Runnable {
                                            state.global.keystrokeVolume = it
                                            state.saveGlobalState()
                                        }).start()
                                    })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("提示音效")
                                    Slider(value = state.typingWord.soundTipsVolume, onValueChange = {
                                        Thread(Runnable {
                                            state.typingWord.soundTipsVolume = it
                                        }).start()
                                    })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("单词发音")
                                    Slider(value = state.global.audioVolume, onValueChange = {
                                        Thread(Runnable {
                                            state.global.audioVolume = it
                                            state.saveGlobalState()
                                        }).start()
                                    })
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("视频播放")
                                    Slider(value = state.global.videoVolume, onValueChange = {
                                        Thread(Runnable {
                                            state.global.videoVolume = it
                                            state.saveGlobalState()
                                        }).start()
                                    })
                                }

                            }
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.VolumeUp,
                        contentDescription = "",
                        tint = MaterialTheme.colors.onBackground,
                        modifier = Modifier.size(48.dp, 48.dp).padding(top = 12.dp, bottom = 12.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp)
                ) {
                    Text("自动发音", color = MaterialTheme.colors.onBackground)
                    Spacer(Modifier.width(35.dp))
                    var expanded by remember { mutableStateOf(false) }
                    val selectedText = when (state.typingWord.pronunciation) {
                        "us" -> "美音"
                        "uk" -> "英音"
                        "jp" -> "日语"
                        else -> "关闭"
                    }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier
                                .width(87.dp)
                                .background(Color.Transparent)
                                .border(1.dp, Color.Transparent)
                        ) {
                            Text(text = selectedText)
                            Icon(Icons.Default.ExpandMore, contentDescription = "Localized description")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.width(87.dp)
                                .height(140.dp)
                        ) {
                            if (state.vocabulary.language == "english") {
                                DropdownMenuItem(
                                    onClick = {
                                        scope.launch {
                                            state.typingWord.pronunciation = "uk"
                                            if (!state.isDictation) {
                                                state.saveTypingWordState()
                                            }
                                            expanded = false
                                        }
                                    },
                                    modifier = Modifier.width(87.dp).height(40.dp)
                                ) {
                                    Text("英音")
                                }
                                DropdownMenuItem(
                                    onClick = {
                                        scope.launch {
                                            state.typingWord.pronunciation = "us"
                                            if (!state.isDictation) {
                                                state.saveTypingWordState()
                                            }
                                            expanded = false
                                        }
                                    },
                                    modifier = Modifier.width(87.dp).height(40.dp)
                                ) {
                                    Text("美音")
                                }
                            }

                            if (state.vocabulary.language == "japanese") {
                                DropdownMenuItem(
                                    onClick = {
                                        scope.launch {
                                            state.typingWord.pronunciation = "jp"
                                            if (!state.isDictation) {
                                                state.saveTypingWordState()
                                            }
                                            expanded = false
                                        }
                                    },
                                    modifier = Modifier.width(87.dp).height(40.dp)
                                ) {
                                    Text("日语")
                                }
                            }

                            DropdownMenuItem(
                                onClick = {
                                    scope.launch {
                                        state.typingWord.pronunciation = "false"
                                        if (!state.isDictation) {
                                            state.saveTypingWordState()
                                        }
                                        expanded = false
                                    }
                                },
                                modifier = Modifier.width(87.dp).height(40.dp)
                            ) {
                                Text("关闭")
                            }
                        }

                    }
                }

            }

            VerticalScrollbar(
                style = LocalScrollbarStyle.current.copy(shape = if(isWindows()) RectangleShape else RoundedCornerShape(4.dp)),
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight(),
                adapter = rememberScrollbarAdapter(stateVertical)
            )
        }
    }
}

fun java.awt.Color.toCompose(): Color {
    return Color(red, green, blue)
}

fun Color.toAwt(): java.awt.Color {
    return java.awt.Color(red, green, blue)
}