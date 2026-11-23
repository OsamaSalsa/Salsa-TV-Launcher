package com.example.ui.settings

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class UnsplashWallpaper(
    val id: String,
    val title: String,
    val photoId: String,
    val customThumbUrl: String? = null,
    val customFullUrl: String? = null
) {
    // Small preview thumbnail URL for fast loading in gallery/grid
    val thumbnailUrl: String
        get() = customThumbUrl
            ?: if (photoId.startsWith("http")) photoId
            else "https://images.unsplash.com/photo-$photoId?auto=format&fit=crop&w=360&q=70"

    // High resolution full wallpaper URL downloaded ONLY when selected
    val fullUrl: String
        get() = customFullUrl
            ?: if (photoId.startsWith("http")) photoId
            else "https://images.unsplash.com/photo-$photoId?auto=format&fit=crop&w=1920&q=85"
}

data class WallpaperCategory(
    val id: String,
    val titleEn: String,
    val titleAr: String,
    val wallpapers: List<UnsplashWallpaper>
)

val onlineWallpaperCategories = listOf(
    WallpaperCategory(
        id = "animals",
        titleEn = "Animals",
        titleAr = "حيوانات",
        wallpapers = listOf(
            UnsplashWallpaper("anim_1", "Leopard in Wild", "1546182990-dffeafbe841d"),
            UnsplashWallpaper("anim_3", "Golden Retriever", "1552053831-71594a27632d"),
            UnsplashWallpaper("anim_4", "Playful Dog Portrait", "1543466835-00a7907e9de1"),
            UnsplashWallpaper("anim_5_new", "Majestic Bengal Tiger", "1561731216-c3a4d99437d5"),
            UnsplashWallpaper("anim_6_new", "Giant Panda Bear", "1564349683136-77e08dba1ef7"),
            UnsplashWallpaper("anim_7", "Deep Sea Jellyfish", "1535591273668-578e31182c4f"),
            UnsplashWallpaper("anim_8", "African Lion Pride", "1534567153574-2b12153a87f0"),
            UnsplashWallpaper("anim_9", "Wild Elephant Sunset", "1557050543-4d5f4e07ef46"),
            UnsplashWallpaper("anim_10", "Red Fox in Forest", "1474511320723-9a56873867b5"),
            UnsplashWallpaper("anim_11", "Curious Black Cat", "1517849845537-4d257902454a"),
            UnsplashWallpaper("anim_12", "Little Puppy Face", "1507146426996-ef05306b995a"),
            UnsplashWallpaper("anim_13", "Flying Eagle", "1501705388883-4ed8a543392c"),
            UnsplashWallpaper("anim_14", "Sea Turtle Swimming", "1437622368342-7a3d73a34c8f"),
            UnsplashWallpaper("anim_15", "Cute Pug Dog", "1518020382113-a7e8fc38eac9"),
            UnsplashWallpaper("anim_16", "Royal White Tiger", "1549480017-d76466a4b7e8"),
            UnsplashWallpaper("anim_17", "Panda Eating Bamboo", "1527118732049-c88155f2107c"),
            UnsplashWallpaper("anim_18", "Himalayan Snow Leopard", "1456926631375-92c8ce872def"),
            UnsplashWallpaper("anim_19", "Tropical Macaw Parrot", "1552728089-57bdde30beb3"),
            UnsplashWallpaper("anim_20", "Arctic Timber Wolf", "1564466809058-bf4114d55352"),
            UnsplashWallpaper("anim_21", "Running Wild Horse", "1553284965-83fd3e82fa5a"),
            UnsplashWallpaper("anim_22", "Emerald Peacock Plume", "1516467508483-a7212febe31a"),
            UnsplashWallpaper("anim_23", "Pink Flamingo Flock", "1497206365907-f5e630693df0")
        )
    ),
    WallpaperCategory(
        id = "architecture",
        titleEn = "Architecture & Interior",
        titleAr = "عمارة وتصميم",
        wallpapers = listOf(
            UnsplashWallpaper("arch_1", "Modern Interior Living", "1513694203232-719a280e022f"),
            UnsplashWallpaper("arch_2", "Glass Skyscraper", "1486406146926-c627a92ad1ab"),
            UnsplashWallpaper("arch_3", "Minimalist Luxury Villa", "1512917774080-9991f1c4c750"),
            UnsplashWallpaper("arch_4", "Modern House Design", "1600585154340-be6161a56a0c"),
            UnsplashWallpaper("arch_5", "Architectural Curves", "1600596542815-ffad4c1539a9"),
            UnsplashWallpaper("arch_6", "Concrete Geometric Art", "1503387762-592deb58ef4e"),
            UnsplashWallpaper("arch_7", "Illuminated Facade", "1513584684374-8bab748fbf90"),
            UnsplashWallpaper("arch_8", "Symmetrical Metal Waves", "1487958449943-2429e8be8625"),
            UnsplashWallpaper("arch_9", "Luxury Penthouse View", "1600607687939-ce8a6c25118c"),
            UnsplashWallpaper("arch_10", "Cozy Warm Living Room", "1618221195710-dd6b41faaea6"),
            UnsplashWallpaper("arch_11", "Nordic Resort Cabin", "1542314831-068cd1dbfeeb")
        )
    ),
    WallpaperCategory(
        id = "experimental",
        titleEn = "Experimental & Art",
        titleAr = "تجريبي وفني",
        wallpapers = listOf(
            UnsplashWallpaper("exp_1", "Neon Light Lines", "1518709268805-4e9042af9f23"),
            UnsplashWallpaper("exp_2", "Abstract Fluid Colors", "1541701494587-cb58502866ab"),
            UnsplashWallpaper("exp_3", "Dark Cyberpunk Glow", "1550684848-fac1c5b4e853"),
            UnsplashWallpaper("exp_5_new", "Purple Abstract Waves", "1557672172-298e090bd0f1"),
            UnsplashWallpaper("exp_6_new", "Vibrant Acrylic Swirl", "1579783900882-c0d3dad7b119"),
            UnsplashWallpaper("exp_7", "Colorful Liquid Art", "1549490349-8643362247b5"),
            UnsplashWallpaper("exp_8", "Neon Geometric Portal", "1550684376-efcbd6e3f031"),
            UnsplashWallpaper("exp_9", "Golden Fluid Marble", "1563089145-599997674d42"),
            UnsplashWallpaper("exp_10", "Cyber Gradient Mesh", "1618005182384-a83a8bd57fbe"),
            UnsplashWallpaper("exp_11", "Holographic Silk Flow", "1618005198919-d3d4b5a92ead")
        )
    ),
    WallpaperCategory(
        id = "nature",
        titleEn = "Nature & Landscape",
        titleAr = "طبيعة",
        wallpapers = listOf(
            UnsplashWallpaper("nat_1", "Misty Mountain Forest", "1470071459604-3b5ec3a7fe05"),
            UnsplashWallpaper("nat_2", "Yosemite Valley Lake", "1426604966848-d7adac402bff"),
            UnsplashWallpaper("nat_3", "Green Mountain Valley", "1472214103451-9374bd1c798e"),
            UnsplashWallpaper("nat_4", "Tropical Sunset Beach", "1507525428034-b723cf961d3e"),
            UnsplashWallpaper("nat_5", "Snowy Alpine Peak", "1464822759023-fed622ff2c3b"),
            UnsplashWallpaper("nat_6", "Serene Sunrise Water", "1470240731273-7821a6eeb6bd"),
            UnsplashWallpaper("nat_7", "Emerald Alpine Lake", "1506744038136-46273834b3fb"),
            UnsplashWallpaper("nat_8", "Autumn Forest Road", "1441974231531-c6227db76b6e"),
            UnsplashWallpaper("nat_9", "Waterfall in Jungle", "1432405972618-c60b0225b8f9"),
            UnsplashWallpaper("nat_10", "Sunset Over Canyon", "1501785888041-af3ef285b470"),
            UnsplashWallpaper("nat_11", "Lavender Sunset Field", "1499002238440-d264edd596ec"),
            UnsplashWallpaper("nat_12", "Dolomites Mountain Peaks", "1519681393784-d120267933ba")
        )
    ),
    WallpaperCategory(
        id = "travel",
        titleEn = "Travel & Places",
        titleAr = "سفر ومدن",
        wallpapers = listOf(
            UnsplashWallpaper("trv_1", "Scenic Mountain Pass", "1488646953014-85cb44e25828"),
            UnsplashWallpaper("trv_2", "Paris Eiffel Tower", "1502602898657-3e91760cbb34"),
            UnsplashWallpaper("trv_3", "European Historic City", "1499856871958-5b9627545d1a"),
            UnsplashWallpaper("trv_4", "Dubai Modern Skyline", "1512453979798-5ea266f8880c"),
            UnsplashWallpaper("trv_5", "Northern Lights Aurora", "1534447677768-be436bb09401"),
            UnsplashWallpaper("trv_6", "Sydney Harbor Lights", "1506973035872-a4ec16b8e8d9"),
            UnsplashWallpaper("trv_7", "Santorini Blue Domes", "1570077188670-e3a8d69ac5ff"),
            UnsplashWallpaper("trv_8", "Kyoto Red Torii Gates", "1493976040374-85c8e12f0c0e"),
            UnsplashWallpaper("trv_9", "Venice Grand Canal", "1514890547357-a9ee288728e0"),
            UnsplashWallpaper("trv_10", "Swiss Alps Chalet", "1530122037265-a5f1f91d3b99"),
            UnsplashWallpaper("trv_11", "Pyramids of Giza Sunset", "1503177119275-0aa32b3a9368"),
            UnsplashWallpaper("trv_12", "Taj Mahal Reflection", "1564507592333-c60657eea523"),
            UnsplashWallpaper("trv_13", "Cappadocia Hot Air Balloons", "1518684079-3c830dcef090")
        )
    ),
    WallpaperCategory(
        id = "space",
        titleEn = "Space & Cosmos",
        titleAr = "فضاء وكون",
        wallpapers = listOf(
            UnsplashWallpaper("space_1", "Deep Space Nebula", "1451187580459-43490279c0fa"),
            UnsplashWallpaper("space_2", "Galaxy Stars Cluster", "1506703719100-a0f3a48c0f86"),
            UnsplashWallpaper("space_3", "Cosmic Dust Clouds", "1446776811953-b23d57bd21aa"),
            UnsplashWallpaper("space_4", "Earth Horizon from Orbit", "1614728894747-a83421e2b9c9"),
            UnsplashWallpaper("space_5", "Full Moon Night", "1532693322450-2cb5c511067d"),
            UnsplashWallpaper("space_6", "Solar Corona Eclipse", "1509198397868-475647b2a1e5"),
            UnsplashWallpaper("space_7", "Orion Nebula Stars", "1462331940025-496dfbfc7564"),
            UnsplashWallpaper("space_8", "Astronaut in Space", "1454789548928-9efd52dc4031"),
            UnsplashWallpaper("space_9", "Spiral Galaxy Core", "1543722530-d2c3201371e7"),
            UnsplashWallpaper("space_10", "Pillars of Creation Nebula", "1462332420958-a05d1e002413"),
            UnsplashWallpaper("space_11", "Saturn Ring Horizon", "1614730321146-b6fa6a46bcb4"),
            UnsplashWallpaper("space_12", "Crescent Moon & Stars", "1532767153582-b1a0e5145009")
        )
    ),
    WallpaperCategory(
        id = "oled",
        titleEn = "Dark & OLED",
        titleAr = "داكن و OLED",
        wallpapers = listOf(
            UnsplashWallpaper("oled_1", "OLED Dark Neon Glow", "1550684848-fac1c5b4e853"),
            UnsplashWallpaper("oled_2", "Midnight Waves Pattern", "1508739773434-c26b3d09e071"),
            UnsplashWallpaper("oled_4", "Black Polygon Geometric", "1550745165-9bc0b252726f"),
            UnsplashWallpaper("oled_5", "Smokey Dark Aura", "1517999144091-3d9dca6d1e43"),
            UnsplashWallpaper("oled_6", "Dark Crimson Neon", "1514525253161-7a46d19cd819"),
            UnsplashWallpaper("oled_7", "Abyssal Blue Glow", "1534796636912-3b95b3ab5986"),
            UnsplashWallpaper("oled_8", "Dark Carbon Waves", "1557683316-973673baf926"),
            UnsplashWallpaper("oled_9", "Dark Blue Silk Waves", "1579546929518-9e396f3cc809"),
            UnsplashWallpaper("oled_10", "Shadowy Forest Mist", "1511497584788-876760111969")
        )
    ),
    WallpaperCategory(
        id = "cars",
        titleEn = "Cars & Motion",
        titleAr = "سيارات وسرعة",
        wallpapers = listOf(
            UnsplashWallpaper("car_1", "Supercar Silhouette", "1503376780353-7e6692767b70"),
            UnsplashWallpaper("car_2", "Classic Porsche", "1580273916550-e323be2ae537"),
            UnsplashWallpaper("car_3", "Neon Night Racer", "1511919884226-fd3cad34687c"),
            UnsplashWallpaper("car_4", "Modern Luxury Coupe", "1552519507-da3b142c6e3d"),
            UnsplashWallpaper("car_5", "Highway Speed Lights", "1517524008697-84bbe3c3fd98"),
            UnsplashWallpaper("car_6", "Red Sports Car Night", "1544636331-e26879cd4d9b"),
            UnsplashWallpaper("car_7", "Black Muscle Car", "1492144534655-ae79c964c9d7"),
            UnsplashWallpaper("car_8", "Supercar Track Day", "1542362567-b07e54358753"),
            UnsplashWallpaper("car_9", "Yellow Supercar Drift", "1544829099-b9a0c07fad1a"),
            UnsplashWallpaper("car_10", "Vintage Mustang Classic", "1584345604476-8ec5e12e42dd"),
            UnsplashWallpaper("car_11", "Formula Racing Track", "1568605117036-5fe5e7bab0b7"),
            UnsplashWallpaper("car_12", "Futuristic Concept EV", "1617788138017-80ad40651399")
        )
    ),
    WallpaperCategory(
        id = "ocean",
        titleEn = "Oceans & Water",
        titleAr = "محيطات وبحار",
        wallpapers = listOf(
            UnsplashWallpaper("ocn_2", "Coral Reef Ecosystem", "1544551763-46a013bb70d5"),
            UnsplashWallpaper("ocn_3", "Deep Ocean Trench", "1518837695005-2083093ee35b"),
            UnsplashWallpaper("ocn_4", "Golden Hour Coast", "1505118380757-91f5f5632de0"),
            UnsplashWallpaper("ocn_5", "Aerial Ocean Surf", "1505144808419-1957a94ca61e"),
            UnsplashWallpaper("ocn_6", "Sunset Sea Horizon", "1519046904884-53103b34b206"),
            UnsplashWallpaper("ocn_7", "Tropical Island Cove", "1559128010-7c1ad6e1b6a5"),
            UnsplashWallpaper("ocn_8", "Maldives Overwater Lagoon", "1514282401047-d79a71a590e8"),
            UnsplashWallpaper("ocn_9", "Crashing Emerald Wave", "1502680390469-be75c86b636f")
        )
    ),
    WallpaperCategory(
        id = "cities",
        titleEn = "Cities at Night",
        titleAr = "مدن وليالي",
        wallpapers = listOf(
            UnsplashWallpaper("cit_1", "Tokyo Neon Alley", "1503899036084-c55cdd92da26"),
            UnsplashWallpaper("cit_2", "New York Skyline", "1496442226666-8d4d0e62e6e9"),
            UnsplashWallpaper("cit_3", "Cyberpunk Metropolis", "1519501025264-65ba15a82390"),
            UnsplashWallpaper("cit_4_new", "Golden Gate Bridge Sunset", "1501594907352-04cda38ebc29"),
            UnsplashWallpaper("cit_5", "San Francisco Bay Fog", "1449034446853-66c86144b0ad"),
            UnsplashWallpaper("cit_6", "Hong Kong Victoria Harbor", "1506318137071-a8e063b4bec0"),
            UnsplashWallpaper("cit_7", "London Tower Bridge Night", "1513635269975-59663e0ac1ad"),
            UnsplashWallpaper("cit_8", "Downtown City Lights", "1480714378408-67cf0d13bc1b"),
            UnsplashWallpaper("cit_9", "Singapore Marina Bay", "1525625293386-3f8f99389edd"),
            UnsplashWallpaper("cit_10", "Seoul Neon Street", "1517154421773-0529f29ea451")
        )
    )
)

/**
 * Searches online 16:9 4K wallpapers by keyword (or direct image URL) using Wallhaven & Wikimedia Commons
 * so the user can search and add new wallpapers to the gallery list directly from the app.
 */
suspend fun searchOnlineWallpapers(rawQuery: String): List<UnsplashWallpaper> = withContext(Dispatchers.IO) {
    val query = rawQuery.trim()
    if (query.isBlank()) return@withContext emptyList()

    val results = mutableListOf<UnsplashWallpaper>()

    // 1. If the user pasted a direct http/https image URL
    if (query.startsWith("http://", ignoreCase = true) || query.startsWith("https://", ignoreCase = true)) {
        val id = "custom_url_${query.hashCode().toUInt()}"
        results.add(
            UnsplashWallpaper(
                id = id,
                title = "4K Direct Wallpaper",
                photoId = query,
                customThumbUrl = query,
                customFullUrl = query
            )
        )
        return@withContext results
    }

    // 2. Search Wallhaven 16:9 SFW 4K Wallpapers API
    try {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val apiUrl = "https://wallhaven.cc/api/v1/search?q=$encoded&purity=100&ratios=16x9&sorting=favorites"
        val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 6000
            setRequestProperty("User-Agent", "Mozilla/5.0")
        }
        if (conn.responseCode == 200) {
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val dataArr = json.optJSONArray("data")
            if (dataArr != null) {
                val limit = minOf(dataArr.length(), 10)
                for (i in 0 until limit) {
                    val item = dataArr.optJSONObject(i) ?: continue
                    val whId = item.optString("id")
                    val fullPath = item.optString("path")
                    val thumbs = item.optJSONObject("thumbs")
                    val thumbPath = thumbs?.optString("original")?.ifBlank { thumbs.optString("large") } ?: fullPath
                    val resolution = item.optString("resolution", "4K")
                    if (fullPath.startsWith("http")) {
                        results.add(
                            UnsplashWallpaper(
                                id = "wh_$whId",
                                title = "${query.replaceFirstChar { it.uppercase() }} ($resolution)",
                                photoId = fullPath,
                                customThumbUrl = thumbPath,
                                customFullUrl = fullPath
                            )
                        )
                    }
                }
            }
        }
        conn.disconnect()
    } catch (_: Exception) {
    }

    // 3. Also search Wikimedia Commons high-res landscape photography
    if (results.size < 8) {
        try {
            val encoded = URLEncoder.encode("filetype:bitmap landscape $query", "UTF-8")
            val wikiUrl =
                "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrsearch=$encoded&gsrnamespace=6&gsrlimit=8&prop=imageinfo&iiprop=url|dimensions&iiurlwidth=480&format=json"
            val conn = (URL(wikiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 6000
                readTimeout = 6000
                setRequestProperty("User-Agent", "SalsaTVLauncher/2.1")
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val pages = JSONObject(body).optJSONObject("query")?.optJSONObject("pages")
                if (pages != null) {
                    val keys = pages.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val page = pages.optJSONObject(key) ?: continue
                        val rawTitle = page.optString("title", "")
                            .removePrefix("File:")
                            .substringBeforeLast(".")
                            .replace("_", " ")
                            .take(28)
                            .ifBlank { query }
                        val ii = page.optJSONArray("imageinfo")?.optJSONObject(0) ?: continue
                        val width = ii.optInt("width", 0)
                        val height = ii.optInt("height", 0)
                        if (width < 1280 || width <= height) continue
                        val thumb = ii.optString("thumburl")
                        val full = ii.optString("url")
                        if (full.startsWith("http") && (full.endsWith(".jpg", true) || full.endsWith(".jpeg", true) || full.endsWith(".png", true))) {
                            results.add(
                                UnsplashWallpaper(
                                    id = "wiki_$key",
                                    title = rawTitle,
                                    photoId = full,
                                    customThumbUrl = thumb.ifBlank { full },
                                    customFullUrl = full
                                )
                            )
                        }
                    }
                }
            }
            conn.disconnect()
        } catch (_: Exception) {
        }
    }

    results
}
