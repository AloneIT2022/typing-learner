package dialog

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import components.ConfirmationDelete
import components.createTransferHandler
import data.*
import player.play
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import player.isWindows
import state.AppState
import state.getResourcesFile
import java.awt.Rectangle
import java.io.File
import java.util.*
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileSystemView

/**
 * 链接字幕词库窗口
 * 把字幕词库链接到文档词库
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalSerializationApi::class)
@Composable
fun LinkVocabularyDialog(
    state: AppState,
    close: () -> Unit
) {
    /**
     * 协程构建器
     */
    val scope = rememberCoroutineScope()

    /**
     * 要链接的词库,通常是内置词库
     */
    var vocabulary by remember{ mutableStateOf<MutableVocabulary?>(null) }

    /**
     * 要链接的词库所在的文件夹的绝对路径
     */
    var directoryPath by remember { mutableStateOf("") }

    /**
     * 当前词库链接到字幕词库的字幕的数量
     */
    var linkCounter by remember { mutableStateOf(0) }

    /**
     * 准备链接的单词和字幕
     */
    val prepareLinks = remember { mutableStateMapOf<String, List<ExternalCaption>>() }


    /**
     * 字幕名称
     */
    var subtitlesName by remember { mutableStateOf("") }


    var vocabularyType by remember { mutableStateOf(VocabularyType.DOCUMENT) }
    var vocabularyWrong by remember { mutableStateOf(false) }
    var extractCaptionResultInfo by remember { mutableStateOf("") }
    var saveEnable by remember { mutableStateOf(false) }

    /**
     * 点击【链接】后执行的回调函数
     */
    val import: () -> Unit = {
        if (prepareLinks.isNotEmpty()) {
            vocabulary?.wordList?.forEach { word ->
                val links = prepareLinks[word.value]
                if (!links.isNullOrEmpty()) {
                    word.externalCaptions.addAll(links)
                }
            }
            saveEnable = true
        }
    }

    val clear: () -> Unit = {
        linkCounter = 0
        prepareLinks.clear()
        subtitlesName = ""
        extractCaptionResultInfo = ""
        vocabularyWrong = false
        vocabularyType = VocabularyType.DOCUMENT

    }

    /**
     * 用户选择字幕词库后，用这个函数提取相关信息
     */
    val extractCaption: (File) -> Unit = {
        Thread(Runnable {
            val selectedVocabulary = loadVocabulary(it.absolutePath)
            subtitlesName = if (selectedVocabulary.type == VocabularyType.SUBTITLES) selectedVocabulary.name else ""
            vocabularyType = selectedVocabulary.type
            var linkedCounter = 0

            // 字幕词库或 MKV 词库，字幕保存在单词的 captions 属性中
            if (selectedVocabulary.type != VocabularyType.DOCUMENT) {
                val wordCaptionsMap = HashMap<String, List<Caption>>()
                selectedVocabulary.wordList.forEach { word ->
                    wordCaptionsMap.put(word.value, word.captions)
                }
                vocabulary?.wordList?.forEach { word ->
                    if (wordCaptionsMap.containsKey(word.value.lowercase(Locale.getDefault()))) {
                        val captions = wordCaptionsMap[word.value]
                        val links = mutableListOf<ExternalCaption>()
                        // 用于预览
                        // 字幕最多3条，这个 counter 是剩余的数量
                        var counter = 3 - word.externalCaptions.size
                        if (counter in 1..3) {
                            captions?.forEachIndexed { _, caption ->

                                val externalCaption = ExternalCaption(
                                    selectedVocabulary.relateVideoPath,
                                    selectedVocabulary.subtitlesTrackId,
                                    subtitlesName,
                                    caption.start,
                                    caption.end,
                                    caption.content
                                )

                                if (counter != 0) {
                                    if (!word.externalCaptions.contains(externalCaption)&& !links.contains(externalCaption)) {
                                        links.add(externalCaption)
                                        counter--
                                    } else {
                                        linkedCounter++
                                    }
                                }
                            }
                        } else {

                            // 字幕已经有3条了，查询是否有一样的
                            captions?.forEachIndexed { _, caption ->
                                val externalCaption = ExternalCaption(
                                    selectedVocabulary.relateVideoPath,
                                    selectedVocabulary.subtitlesTrackId,
                                    subtitlesName,
                                    caption.start,
                                    caption.end,
                                    caption.content
                                )

                                if (word.externalCaptions.contains(externalCaption)) {
                                    linkedCounter++
                                }
                            }
                        }
                        if (links.isNotEmpty()) {
                            prepareLinks.put(word.value, links)
                            linkCounter += links.size
                        }

                    }
                }

            }else {
            // 文档词库，字幕保存在单词的 externalCaptions 属性中
                val wordCaptionsMap = HashMap<String, List<ExternalCaption>>()
                selectedVocabulary.wordList.forEach { word ->
                    wordCaptionsMap.put(word.value, word.externalCaptions)
                }
                vocabulary?.wordList?.forEach { word ->
                    if (wordCaptionsMap.containsKey(word.value.lowercase(Locale.getDefault()))) {
                        val externalCaptions = wordCaptionsMap[word.value]
                        val links = mutableListOf<ExternalCaption>()
//                        // 用于预览
                        // 字幕最多3条，这个 counter 是剩余的数量
                        var counter = 3 - word.externalCaptions.size
                        if (counter in 1..3) {
                            externalCaptions?.forEachIndexed { _, externalCaption ->
                                if (counter != 0) {
                                    if (!word.externalCaptions.contains(externalCaption)&& !links.contains(externalCaption)) {
                                        links.add(externalCaption)
                                        counter--
                                    } else {
                                        linkedCounter++
                                    }
                                }
                            }
                        } else {
                            // 字幕已经有3条了，查询是否有一样的
                            externalCaptions?.forEachIndexed { _, externalCaption ->
                                if (word.externalCaptions.contains(externalCaption)) {
                                    linkedCounter++
                                }
                            }
                        }
                        if (links.isNotEmpty()) {
                            prepareLinks.put(word.value, links)
                            linkCounter += links.size
                        }

                    }
                }
            }

            // previewWords isEmpty 有两种情况：
            // 1. 已经链接了一次。
            // 2. 没有匹配的字幕
            if (prepareLinks.isEmpty()) {
                extractCaptionResultInfo = if (linkedCounter == 0) {
                    "没有匹配的字幕，请重新选择"
                } else {
                    "${selectedVocabulary.name} 有${linkedCounter}条相同的字幕已经链接，请重新选择"
                }
                vocabularyWrong = true
            }
        }).start()
    }

    /**
     * 处理输入的文件
     */
    val handleInputFile:(File) -> Unit = {file ->
        if(vocabulary == null){
            vocabulary =  MutableVocabulary(loadVocabulary(file.absolutePath))
            directoryPath = file.parentFile.absolutePath
        }else{
            extractCaption(file)
        }
    }



    Dialog(
        title = "链接字幕",
        icon = painterResource("logo/logo.png"),
        onCloseRequest = {
            clear()
            close()
        },
        resizable = true,
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(600.dp, 650.dp)
        ),
    ) {


        /**  处理拖放文件的函数 */
        val transferHandler = createTransferHandler(
            showWrongMessage = { message ->
                JOptionPane.showMessageDialog(window, message)
            },
            parseImportFile = { files ->
                val file = files.first()
                scope.launch {
                    if (file.extension == "json") {
                        handleInputFile(file)
                    } else {
                        JOptionPane.showMessageDialog(window, "词库的格式不正确")
                    }


                }
            }
        )
        window.transferHandler = transferHandler

        /** 保存词库 */
        val save:() -> Unit = {
            Thread(Runnable {

                val fileChooser = state.futureFileChooser.get()
                fileChooser.dialogType = JFileChooser.SAVE_DIALOG
                fileChooser.dialogTitle = "保存词库"
                val myDocuments = FileSystemView.getFileSystemView().defaultDirectory.path
                val appVocabulary = getResourcesFile("vocabulary")
                val parent = if(directoryPath.startsWith(appVocabulary.absolutePath)){
                    myDocuments
                }else directoryPath
                fileChooser.selectedFile = File("$parent${File.separator}${vocabulary?.name}.json")
                val userSelection = fileChooser.showSaveDialog(window)
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    val fileToSave = fileChooser.selectedFile
                    if (vocabulary != null) {
                        vocabulary!!.name = fileToSave.nameWithoutExtension
                        saveVocabulary(vocabulary!!.serializeVocabulary, fileToSave.absolutePath)
                    }
                    vocabulary = null
                    fileChooser.selectedFile = null
                    close()
                }
                clear()
            }).start()
        }

        /** 选择内置词库*/
        val openVocabulary:(String) -> Unit = {title ->
            vocabularyWrong = false
            state.loadingFileChooserVisible = true
            Thread(Runnable {
                val fileChooser = state.futureFileChooser.get()
                fileChooser.dialogTitle = title
                fileChooser.fileSystemView = FileSystemView.getFileSystemView()
                if(title =="选择内置词库"){
                    fileChooser.currentDirectory = getResourcesFile("vocabulary")
                }else{
                    fileChooser.currentDirectory = FileSystemView.getFileSystemView().defaultDirectory
                }
                fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
                fileChooser.selectedFile = null
                if (fileChooser.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                    val file = fileChooser.selectedFile
                    handleInputFile(file)
                    state.loadingFileChooserVisible = false
                } else {
                    state.loadingFileChooserVisible = false
                }
            }).start()
        }

        WindowDraggableArea {
            Surface(
                elevation = 5.dp,
                shape = RectangleShape,
            ) {
                Box(Modifier.fillMaxSize()) {
                    Divider(Modifier.align(Alignment.TopCenter))
                    if (prepareLinks.isEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize().align(Alignment.Center)
                        ) {
                            // 当前词库已经链接的外部字幕
                            val externalNameMap = mutableMapOf<String, Int>()
                            vocabulary?.wordList?.forEach { word ->
                                word.externalCaptions.forEach { externalCaption ->
                                    // 视频词库
                                    if (externalCaption.relateVideoPath.isNotEmpty()) {
                                        var counter = externalNameMap[externalCaption.relateVideoPath]
                                        if (counter == null) {
                                            externalNameMap[externalCaption.relateVideoPath] = 1
                                        } else {
                                            counter++
                                            externalNameMap[externalCaption.relateVideoPath] = counter
                                        }
                                        // 字幕词库
                                    } else if (externalCaption.subtitlesName.isNotEmpty()) {
                                        var counter = externalNameMap[externalCaption.subtitlesName]
                                        if (counter == null) {
                                            externalNameMap[externalCaption.subtitlesName] = 1
                                        } else {
                                            counter++
                                            externalNameMap[externalCaption.subtitlesName] = counter
                                        }
                                    }

                                }
                            }

                            Column(Modifier.width(IntrinsicSize.Max)) {
                                if(vocabulary != null){
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                                    ) { Text(vocabulary!!.name) }
                                    val bottom = if(externalNameMap.isEmpty()) 50.dp else 0.dp
                                    Divider(Modifier.padding(bottom = bottom))
                                }
                                if (externalNameMap.isNotEmpty()) {
                                    Column {
                                        var showConfirmationDialog by remember { mutableStateOf(false) }
                                        externalNameMap.forEach { (path, count) ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                val name = File(path).nameWithoutExtension
                                                Text(
                                                    text = name,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.width(250.dp).padding(end = 10.dp)
                                                )
                                                Text("$count", modifier = Modifier.width(60.dp))
                                                IconButton(onClick = { showConfirmationDialog = true }) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Delete,
                                                        contentDescription = "",
                                                        tint = MaterialTheme.colors.onBackground
                                                    )
                                                }
                                                if (showConfirmationDialog) {
                                                    ConfirmationDelete(
                                                        message = "确定要删除 $name 的所有字幕吗?",
                                                        confirm = {
                                                            vocabulary?.wordList?.forEach { word ->
                                                                val tempList = mutableListOf<ExternalCaption>()
                                                                word.externalCaptions.forEach { externalCaption ->
                                                                    if (externalCaption.relateVideoPath == path || externalCaption.subtitlesName == path) {
                                                                        tempList.add(externalCaption)
                                                                    }
                                                                }
                                                                word.externalCaptions.removeAll(tempList)
                                                            }
                                                            if (
//                                                                relateVideoPath == path ||
                                                                subtitlesName == path) {
                                                                vocabularyWrong = false
                                                            }
                                                            showConfirmationDialog = false
                                                            saveEnable = true
                                                        },
                                                        close = { showConfirmationDialog = false }
                                                    )
                                                }

                                            }
                                        }
                                    }
                                    Divider()
                                }

                            }
                            if (vocabularyWrong) {
                                if (extractCaptionResultInfo.isNotEmpty()) {
                                    Text(
                                        text = extractCaptionResultInfo,
                                        color = Color.Red,
                                        modifier = Modifier.width(360.dp).padding(top = 20.dp, bottom = 20.dp)
                                    )
                                }
                                if (vocabularyType == VocabularyType.DOCUMENT) {
                                    Text(
                                        "词库的类型错误，请选择从 SRT 或 MKV 生成的词库文件",
                                        color = Color.Red,
                                        modifier = Modifier.width(360.dp).padding(top = 20.dp, bottom = 20.dp)
                                    )
                                }

                            }

                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(onClick = {
                                    openVocabulary("选择词库")
                                }) {
                                    Text("选择词库")
                                }
                                Spacer(Modifier.width(20.dp))
                                OutlinedButton(onClick = {
                                    openVocabulary("选择内置词库")
                                }) {
                                    Text("选择内置词库")
                                }
                                Spacer(Modifier.width(20.dp))
                                OutlinedButton(onClick = { save() }, enabled = saveEnable) {
                                    Text("保存")
                                }
                                Spacer(Modifier.width(20.dp))
                                OutlinedButton(onClick = {
                                    clear()
                                    close()
                                }) {
                                    Text("取消")
                                }
                            }
                        }

                        Text("提示：不要把词库保存到应用程序的安装目录", modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp))
                    } else {
                        Column(Modifier.fillMaxSize().align(Alignment.Center)) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp)
                            ) {
                                Text("总共${prepareLinks.size}个单词,${linkCounter}条字幕")
                            }
                            Divider()
                            Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                                val scrollState = rememberLazyListState()
                                LazyColumn(Modifier.fillMaxSize(), scrollState) {

                                    items(prepareLinks.toList()) { (word, captions) ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
                                                .padding(start = 10.dp, end = 10.dp)
                                        ) {

                                            Text(text = word, modifier = Modifier.width(150.dp))
                                            Divider(Modifier.width(1.dp).fillMaxHeight())
                                            Column(verticalArrangement = Arrangement.Center) {
                                                captions.forEachIndexed { index, externalCaption ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = "${index + 1}. ${externalCaption.content}",
                                                            modifier = Modifier.padding(5.dp)
                                                        )
                                                        val caption = Caption(externalCaption.start,externalCaption.end,externalCaption.content)
                                                        val playTriple =
                                                            Triple(caption, externalCaption.relateVideoPath, externalCaption.subtitlesTrackId)
                                                        val playerBounds by remember {
                                                            mutableStateOf(
                                                                Rectangle(
                                                                    0,
                                                                    0,
                                                                    540,
                                                                    303
                                                                )
                                                            )
                                                        }
                                                        var isPlaying by remember { mutableStateOf(false) }
                                                        IconButton(
                                                            onClick = {},
                                                            modifier = Modifier
                                                                .onPointerEvent(PointerEventType.Press) { pointerEvent ->
                                                                    val location =
                                                                        pointerEvent.awtEventOrNull?.locationOnScreen
                                                                    if (location != null) {
                                                                        if (!isPlaying) {
                                                                            isPlaying = true
                                                                            playerBounds.x = location.x - 270 + 24
                                                                            playerBounds.y = location.y - 320
                                                                            val file = File( externalCaption.relateVideoPath)
                                                                            if (file.exists()) {
                                                                                scope.launch {
                                                                                    play(
                                                                                        window = state.videoPlayerWindow,
                                                                                        setIsPlaying = {
                                                                                            isPlaying = it
                                                                                        },
                                                                                        volume = state.global.videoVolume,
                                                                                        playTriple = playTriple,
                                                                                        videoPlayerComponent = state.videoPlayerComponent,
                                                                                        bounds = playerBounds
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                        ) {
                                                            Icon(
                                                                Icons.Filled.PlayArrow,
                                                                contentDescription = "Localized description",
                                                                tint = MaterialTheme.colors.primary
                                                            )
                                                        }
                                                    }
                                                }

                                            }
                                        }
                                        Divider()
                                    }
                                }
                                VerticalScrollbar(
                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                                    adapter = rememberScrollbarAdapter(scrollState = scrollState),
                                    style = LocalScrollbarStyle.current.copy(shape = if(isWindows()) RectangleShape else RoundedCornerShape(4.dp)),
                                )
                            }

                            Divider()
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 5.dp)
                            ) {
                                OutlinedButton(onClick = {
                                    import()
                                    clear()
                                }) {
                                    Text("链接")
                                }
                                Spacer(Modifier.width(20.dp))
                                OutlinedButton(onClick = { clear() }) {
                                    Text("取消")
                                }
                            }
                        }
                    }
                }


            }
        }
    }
}