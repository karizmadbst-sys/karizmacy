package com.example.data

enum class PresetArtwork(val uri: String, val labelEn: String, val labelAr: String) {
    CYBER_DRIFT("preset:cyber_drift", "Neon Drift", "انجراف نيون"),
    NOVA_STRIKE("preset:nova_strike", "Orbital Vanguard", "طليعة المدار"),
    ECLIPSE_REALM("preset:eclipse_realm", "Lunar Eclipse", "كسوف القمر"),
    NEON_APPS("preset:neon_apps", "Cyber Prism", "منشور سيبراني"),
    KARIZMA_EMBLEM("preset:karizma_emblem", "Karizma Shield", "درع كاريزما");

    companion object {
        fun fromUri(uri: String?): PresetArtwork? =
            entries.firstOrNull { it.uri == uri }
    }
}

object SeedCatalogData {
    val starterItems: List<CatalogItemDraft> = listOf(
        // --- REAL ANDROID GAMES WITH OFFICIAL DOWNLOAD LINKS ---
        CatalogItemDraft(
            title = "Mindustry",
            titleAr = "مايندستري — Mindustry",
            itemType = ItemType.GAME,
            category = StoreCategory.STRATEGY,
            developer = "Anuken",
            summary = "Open-ended factory automation & sci-fi tower defense across alien planets.",
            summaryAr = "لعبة استراتيجية لبناء المصانع والدفاع عن الأبراج عبر كواكب فضائية.",
            description = "Build intricate supply chains of conveyor belts to feed ammunition into your turrets, produce advanced building materials, and defend your core against waves of robotic enemies. Supports cross-platform multiplayer, custom maps, and campaign progression across two massive planets.",
            descriptionAr = "قم ببناء شبكات إمداد متطورة لتزويد أبراج الدفاع بالذخيرة، وإنتاج مواد بناء متقدمة لحماية قاعدتك من موجات الأعداء الآليين. تدعم اللعب الجماعي عبر المنصات وخرائط مخصصة وحملة كوكبية ضخمة.",
            version = "146.0",
            fileSize = "78 MB",
            downloadUrl = "https://github.com/Anuken/Mindustry/releases",
            coverImageUrl = PresetArtwork.NOVA_STRIKE.uri,
            screenshotUrls = listOf(
                PresetArtwork.NOVA_STRIKE.uri,
                PresetArtwork.CYBER_DRIFT.uri,
                PresetArtwork.ECLIPSE_REALM.uri
            ),
            rating = 4.9,
            featured = true,
            published = true
        ),
        CatalogItemDraft(
            title = "SuperTuxKart",
            titleAr = "سوبر تاكس كارت — SuperTuxKart",
            itemType = ItemType.GAME,
            category = StoreCategory.RACING,
            developer = "STK Team",
            summary = "High-speed 3D arcade kart racing with nitro boosts, tracks, and online multiplayer.",
            summaryAr = "سباق سيارات أركيد ثلاثي الأبعاد بسرعات فائقة وتنافس جماعي عبر الإنترنت.",
            description = "Race across more than 20 high-octane tracks featuring underwater tunnels, futuristic circuits, and volcanic arenas. Master sharp drifting, collect nitro power-ups, compete in Grand Prix cups, or challenge players worldwide in real-time online multiplayer races.",
            descriptionAr = "تسابق عبر أكثر من 20 حلبة مليئة بالإثارة تشمل أنفاقاً تحت الماء ومضامير مستقبلية. أتقن الانجراف السريع واجمع معززات النيترو وتنافس عالمياً في سباقات جماعية مباشرة.",
            version = "1.4",
            fileSize = "615 MB",
            downloadUrl = "https://supertuxkart.net/Download",
            coverImageUrl = PresetArtwork.CYBER_DRIFT.uri,
            screenshotUrls = listOf(
                PresetArtwork.CYBER_DRIFT.uri,
                PresetArtwork.NOVA_STRIKE.uri
            ),
            rating = 4.7,
            featured = true,
            published = true
        ),
        CatalogItemDraft(
            title = "Shattered Pixel Dungeon",
            titleAr = "الزنزانة المحطمة — Pixel Dungeon",
            itemType = ItemType.GAME,
            category = StoreCategory.RPG,
            developer = "Shattered Pixel",
            summary = "Deep tactical roguelike dungeon crawler with procedural worlds and 5 unique heroes.",
            summaryAr = "لعبة تقمص أدوار استراتيجية بعوالم متجددة وخمسة أبطال بقدرات فريدة.",
            description = "Explore endlessly replayable dungeons filled with ancient artifacts, elemental wands, deadly bosses, and hidden chambers. Every run generates a fresh floor layout, encouraging smart positioning, alchemy crafting, and distinct hero talent builds.",
            descriptionAr = "استكشف دهاليز متجددة مليئة بالقطع الأثرية والعصي السحرية والزعماء الأقوياء. كل جولة تولّد خريطة جديدة بالكامل لتجربة تكتيكية لا تتكرر.",
            version = "2.5.4",
            fileSize = "24 MB",
            downloadUrl = "https://github.com/00-Evan/shattered-pixel-dungeon/releases",
            coverImageUrl = PresetArtwork.ECLIPSE_REALM.uri,
            screenshotUrls = listOf(
                PresetArtwork.ECLIPSE_REALM.uri,
                PresetArtwork.NOVA_STRIKE.uri
            ),
            rating = 4.9,
            featured = true,
            published = true
        ),
        CatalogItemDraft(
            title = "Unciv",
            titleAr = "أنسيف — Unciv",
            itemType = ItemType.GAME,
            category = StoreCategory.STRATEGY,
            developer = "Yair Morgenstern",
            summary = "Build your civilization, research technologies, and lead armies in classic 4X strategy.",
            summaryAr = "ابنِ حضارتك وطوّر العلوم وقد الجيوش في لعبة استراتيجية كبرى شاملة.",
            description = "Lead a civilization from the ancient era to the futuristic space age. Forge diplomatic alliances, expand cities across hex-based continents, customize gameplay with community mods, and enjoy crisp, battery-friendly turn-based strategy anywhere.",
            descriptionAr = "قُد حضارتك من العصور القديمة حتى عصر الفضاء المستقبلي. شكّل تحالفات دبلوماسية ووسّع مدنك واستمتع بدعم التعديلات المجتمعية وتجربة استراتيجية عميقة.",
            version = "4.14.12",
            fileSize = "38 MB",
            downloadUrl = "https://github.com/yairm210/Unciv/releases",
            coverImageUrl = PresetArtwork.ECLIPSE_REALM.uri,
            screenshotUrls = listOf(
                PresetArtwork.ECLIPSE_REALM.uri,
                PresetArtwork.NEON_APPS.uri
            ),
            rating = 4.8,
            featured = false,
            published = true
        ),
        CatalogItemDraft(
            title = "PPSSPP",
            titleAr = "محاكي PPSSPP للألعاب",
            itemType = ItemType.GAME,
            category = StoreCategory.ARCADE,
            developer = "Henrik Rydgard",
            summary = "High-definition PSP gaming engine with Vulkan rendering, upscaling & controller support.",
            summaryAr = "محرك تشغيل ألعاب عالي الدقة مع دعم Vulkan وتحسين الرسوميات وأذرع التحكم.",
            description = "Run your classic portable game backups in crisp HD resolution with anisotropic filtering, customizable touch controls, Bluetooth gamepad mapping, save states anywhere, and hardware-accelerated Vulkan performance.",
            descriptionAr = "شغّل ألعابك المحمولة بدقة عالية مع تحسين الأنسجة وتخصيص أزرار اللمس ودعم أذرع التحكم اللاسلكية وحفظ التقدم في أي لحظة بأداء فائق.",
            version = "1.18.1",
            fileSize = "44 MB",
            downloadUrl = "https://www.ppsspp.org/download/",
            coverImageUrl = PresetArtwork.NOVA_STRIKE.uri,
            screenshotUrls = listOf(
                PresetArtwork.NOVA_STRIKE.uri,
                PresetArtwork.CYBER_DRIFT.uri
            ),
            rating = 4.8,
            featured = false,
            published = true
        ),
        CatalogItemDraft(
            title = "RetroArch",
            titleAr = "ريترو آرك — RetroArch",
            itemType = ItemType.GAME,
            category = StoreCategory.ACTION,
            developer = "Libretro",
            summary = "Unified multi-system gaming frontend with shaders, netplay, and achievements.",
            summaryAr = "منصة ألعاب موحدة متعددة الأنظمة مع مؤثرات بصرية ولعب جماعي وإنجازات.",
            description = "Experience a sleek, unified gaming interface supporting dozens of classic game engines, real-time rewind, CRT/neon GPU shaders, low-latency run-ahead frames, and synchronized netplay sessions with friends.",
            descriptionAr = "استمتع بواجهة ألعاب موحدة تدعم عشرات المحركات الكلاسيكية مع إمكانية الإرجاع الفوري ومؤثرات الرسوميات المتقدمة واللعب الجماعي عبر الشبكة.",
            version = "1.19.1",
            fileSize = "162 MB",
            downloadUrl = "https://www.retroarch.com/?page=platforms",
            coverImageUrl = PresetArtwork.CYBER_DRIFT.uri,
            screenshotUrls = listOf(
                PresetArtwork.CYBER_DRIFT.uri,
                PresetArtwork.KARIZMA_EMBLEM.uri
            ),
            rating = 4.6,
            featured = false,
            published = true
        ),

        // --- REAL ANDROID APPLICATIONS WITH OFFICIAL DOWNLOAD LINKS ---
        CatalogItemDraft(
            title = "VLC for Android",
            titleAr = "مشغل الوسائط VLC",
            itemType = ItemType.APP,
            category = StoreCategory.MEDIA,
            developer = "VideoLAN",
            summary = "Ultimate hardware-accelerated video & hi-res audio player for every media format.",
            summaryAr = "أقوى مشغل فيديو وصوت عالي الدقة يدعم جميع الصيغ والترجمات بدون إعلانات.",
            description = "Play MKV, MP4, FLAC, AV1, network streams, and local libraries effortlessly with zero ads or tracking. Includes multi-track audio, customizable subtitles, 10-band equalizer, Picture-in-Picture mode, and gesture controls.",
            descriptionAr = "شغّل جميع صيغ الفيديو والصوت والبث الشبكي بسلاسة تامة بدون إعلانات. يضم معادل صوت احترافي ودعم الترجمات المتعددة ووضع صورة داخل صورة.",
            version = "3.5.7",
            fileSize = "49 MB",
            downloadUrl = "https://www.videolan.org/vlc/download-android.html",
            coverImageUrl = PresetArtwork.NEON_APPS.uri,
            screenshotUrls = listOf(
                PresetArtwork.NEON_APPS.uri,
                PresetArtwork.KARIZMA_EMBLEM.uri
            ),
            rating = 4.8,
            featured = true,
            published = true
        ),
        CatalogItemDraft(
            title = "Termux",
            titleAr = "تيرمكس — Termux",
            itemType = ItemType.APP,
            category = StoreCategory.TOOLS,
            developer = "Termux Team",
            summary = "Powerful Linux terminal emulator and developer package environment on Android.",
            summaryAr = "محاكي طرفية لينكس متكامل وبيئة تطوير برمجية قوية على أندرويد.",
            description = "Access a complete command-line workspace directly on your phone or tablet. Install Python, Node.js, Git, OpenSSH, and Clang via pkg/apt with zero root required and full hardware keyboard shortcut support.",
            descriptionAr = "احصل على بيئة سطر أوامر كاملة مباشرة على هاتفك. ثبّت بايثون وGit وOpenSSH وأدوات التطوير بسهولة بدون الحاجة لصلاحيات الروت.",
            version = "0.118.1",
            fileSize = "105 MB",
            downloadUrl = "https://github.com/termux/termux-app/releases",
            coverImageUrl = PresetArtwork.NEON_APPS.uri,
            screenshotUrls = listOf(
                PresetArtwork.NEON_APPS.uri,
                PresetArtwork.CYBER_DRIFT.uri
            ),
            rating = 4.9,
            featured = false,
            published = true
        ),
        CatalogItemDraft(
            title = "Krita",
            titleAr = "كريتا للرسم الرقمي — Krita",
            itemType = ItemType.APP,
            category = StoreCategory.PRODUCTIVITY,
            developer = "KDE Community",
            summary = "Professional digital painting, concept art & 2D animation studio for Android.",
            summaryAr = "استوديو احترافي للرسم الرقمي وتصميم الشخصيات والرسوم المتحركة ثنائية الأبعاد.",
            description = "Unleash desktop-grade digital illustration with over 100 customizable brushes, stylus pressure sensitivity, vector layers, wrap-around textures, and frame-by-frame animation timelines built for artists and game designers.",
            descriptionAr = "أطلق إبداعك بأكثر من 100 فرشاة احترافية ودعم القلم الضوئي والطبقات المتعددة وأدوات التحريك المصممة للرسامين ومصممي الألعاب.",
            version = "5.2.6",
            fileSize = "175 MB",
            downloadUrl = "https://krita.org/en/download/",
            coverImageUrl = PresetArtwork.ECLIPSE_REALM.uri,
            screenshotUrls = listOf(
                PresetArtwork.ECLIPSE_REALM.uri,
                PresetArtwork.NEON_APPS.uri
            ),
            rating = 4.7,
            featured = false,
            published = true
        ),
        CatalogItemDraft(
            title = "Obsidian",
            titleAr = "أوبسيديان للملاحظات — Obsidian",
            itemType = ItemType.APP,
            category = StoreCategory.PRODUCTIVITY,
            developer = "Dynalist Inc.",
            summary = "Private, ultra-fast Markdown second brain with interactive graph view & plugins.",
            summaryAr = "تطبيق ملاحظات ذكي وسريع بصيغة Markdown مع خريطة روابط تفاعلية وإضافات.",
            description = "Organize game design docs, study notes, and personal projects in interconnected Markdown vaults stored cleanly on your device. Explore links visually with Graph View, split-pane workspaces, and hundreds of community themes.",
            descriptionAr = "نظّم أفكارك ومشاريعك وملاحظاتك في مساحة عمل مترابطة وسريعة مع عرض بياني تفاعلي ومئات القوالب والإضافات الاحترافية.",
            version = "1.7.7",
            fileSize = "34 MB",
            downloadUrl = "https://obsidian.md/download",
            coverImageUrl = PresetArtwork.KARIZMA_EMBLEM.uri,
            screenshotUrls = listOf(
                PresetArtwork.KARIZMA_EMBLEM.uri,
                PresetArtwork.NEON_APPS.uri
            ),
            rating = 4.8,
            featured = false,
            published = true
        ),
        CatalogItemDraft(
            title = "Proton VPN",
            titleAr = "بروتون في بي إن — Proton VPN",
            itemType = ItemType.APP,
            category = StoreCategory.SECURITY,
            developer = "Proton AG",
            summary = "High-speed Swiss-engineered encrypted VPN for low-latency gaming and privacy.",
            summaryAr = "شبكة خاصة مشفرة فائقة السرعة لحماية الخصوصية وتقليل زمن الاستجابة للألعاب.",
            description = "Protect your connection on public Wi-Fi and secure your gaming sessions with audited open-source WireGuard encryption, NetShield ad-blocking, strict no-logs policy, and ultra-fast global servers.",
            descriptionAr = "احمِ اتصالك على الشبكات العامة وأمّن جلسات اللعب بتشفير WireGuard السريع وسياسة عدم حفظ السجلات وخوادم عالمية عالية الأداء.",
            version = "5.7.93",
            fileSize = "62 MB",
            downloadUrl = "https://protonvpn.com/download-android",
            coverImageUrl = PresetArtwork.NEON_APPS.uri,
            screenshotUrls = listOf(
                PresetArtwork.NEON_APPS.uri,
                PresetArtwork.NOVA_STRIKE.uri
            ),
            rating = 4.7,
            featured = false,
            published = true
        ),
        CatalogItemDraft(
            title = "Discord",
            titleAr = "ديسكورد — Discord",
            itemType = ItemType.APP,
            category = StoreCategory.SOCIAL,
            developer = "Discord Inc.",
            summary = "Crystal-clear gaming voice chat, squad servers, screen sharing & communities.",
            summaryAr = "دردشة صوتية فائقة النقاء للألعاب وخوادم الفرق ومشاركة الشاشة والمجتمعات.",
            description = "Squad up with friends across mobile, PC, and consoles. Enjoy ultra-low-latency voice channels, HD gameplay streaming, custom community servers, rich game status overlays, and instant media sharing.",
            descriptionAr = "تواصل مع فريقك عبر الهاتف والحاسوب ومنصات الألعاب مع قنوات صوتية فائقة الوضوح وبث مباشر للعب وخوادم مجتمعية مخصصة.",
            version = "256.14",
            fileSize = "128 MB",
            downloadUrl = "https://discord.com/download",
            coverImageUrl = PresetArtwork.KARIZMA_EMBLEM.uri,
            screenshotUrls = listOf(
                PresetArtwork.KARIZMA_EMBLEM.uri,
                PresetArtwork.CYBER_DRIFT.uri
            ),
            rating = 4.6,
            featured = false,
            published = true
        )
    )
}
