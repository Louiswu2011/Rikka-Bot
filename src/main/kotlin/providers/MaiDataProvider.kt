package providers

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import maimai.maidata.MaiObjectStats
import maimai.maidata.MaiRecentTrack
import maimai.maidata.MaiTrackDetail
import maimai.maidata.MaiUser
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode

/**Provide Maimai user data, track details, recent records and more.
 *
 * Get [latestToken] after any action to retrieve the latest token.
 */
class MaiDataProvider {

    val homeUrl = "https://maimai.wahlap.com/maimai-mobile/playerData/"
    val recentUrl = "https://maimai.wahlap.com/maimai-mobile/record/"
    val detailUrl = "https://maimai.wahlap.com/maimai-mobile/record/playlogDetail/"
    val searchGenreUrl = "https://maimai.wahlap.com/maimai-mobile/record/musicGenre/search/" // ?genre=(<?>/99)&diff(0-4/99)
    val rankingUrl = "https://maimai.wahlap.com/maimai-mobile/ranking/musicRankingDetail/"   // Unknown params for now
    val trackUrl = "https://maimai.wahlap.com/maimai-mobile/record/musicDetail/"             // Unknown params either

    var latestToken = ""

    // TODO: Get Avatar!!
    /**Get specific user basic data. Returns an instance of MaiUser class.
     *
     * Get [latestToken] after this action to retrieve the latest token.
     *
     * @param id A unique id for each player.
     * @param token A randomized token to validate each action.
     * @since NLBot2021 ver1.0
     */
    suspend fun getMaiUser(id: String, token: String): MaiUser {
        val returns = getResponseStringFromUrl(id, token, homeUrl)

        if (returns[1].isEmpty() || returns[2].isEmpty()) {
            throw Exception() // TODO: Throw a proper exception indicating wrong token or id
        }

        val doc = Jsoup.parse(returns[0])
        val usernameNode = doc.getElementsByClass("name_block f_l f_14")[0].childNode(0) as TextNode
        val ratingNode = doc.getElementsByClass("rating_block f_11")[0].childNode(0) as TextNode
        val maxRatingNode = doc.getElementsByClass("p_r_5 f_11")[0].childNode(0) as TextNode
        val playCountNode = doc.getElementsByClass("m_5 m_t_10 t_r f_12")[0].childNode(0) as TextNode
        val trophyTitleNode = doc.select("div[class=trophy_inner_block f_13]").select("span")
        val starCountNode = doc.getElementsByClass("p_l_10 f_l f_14")[0].childNode(1) as TextNode

        latestToken = returns[2]

        return MaiUser(
            username = usernameNode.wholeText,
            rating = ratingNode.wholeText.toInt(),
            maxRating = maxRatingNode.wholeText.substringAfter("：").toInt(),
            playCount = playCountNode.wholeText,
            trophyTitle = trophyTitleNode.text(),
            starCount = starCountNode.wholeText.removePrefix("×"),
            ""
        )
    }

    /**Get specific track data from recent plays. Returns an instance of MaiTrackDetail class.
     *
     * Get [latestToken] after this action to retrieve the latest token.
     *
     * @param id A unique id for each player.
     * @param token A randomized token to validate each action.
     * @param trackID The track ID to query. Obtainable in [MaiRecentTrack.id]
     * @since NLBot2021 ver1.0
     */
    suspend fun getTrackDetailFromRecent(id: String, token: String, trackID: String): MaiTrackDetail {
        val returns = getResponseStringFromUrl(id, token, "$detailUrl?idx=$trackID")

        if (returns[1].isEmpty() || returns[2].isEmpty()) {
            throw Exception("Wrong ID!")
        }

        latestToken = returns[2]

        val doc = Jsoup.parse(returns[0])

        val noteTable = doc.select("table[class=playlog_notes_detail t_r f_l f_11 f_b]")[0]

        val tapRow = noteTable.select("tr")[1].select("td")
        val holdRow = noteTable.select("tr")[2].select("td")
        val slideRow = noteTable.select("tr")[3].select("td")
        val touchRow = noteTable.select("tr")[4].select("td")
        val breakRow = noteTable.select("tr")[5].select("td")

        val rating = doc.select("table[class=playlog_grade_block m_t_5 f_0]")
            .select("tr")[0]
            .select("td[class=t_r f_12]")
            .select("div[class=playlog_rating_val_block]")
            .text()

        val ratingChanges = doc.select("table[class=playlog_grade_block m_t_5 f_0]")
            .select("tr")[0]
            .select("td[class=t_r f_12]")
            .select("span[class=f_11]")
            .text()

        val baseRating = doc.select("table[class=playlog_grade_block m_t_5 f_0]")
            .select("tr")[1]
            .select("td[class=t_r f_12]")
            .select("div[class=playlog_rating_val_block]")
            .text()

        val matchingGrid = doc.select("div[id=matching]").select("div[class=basic_block p_3 t_c f_11]")

        val matchingPlayers = mutableListOf<String>()

        matchingGrid.forEach {
            matchingPlayers.add(it.text())
        }

        return MaiTrackDetail(
            trackID,
            doc.select("div[class=basic_block m_5 p_5 p_l_10 f_13 break]").text(),
            doc.select("span[class=red f_b v_b]").text(),
            doc.select("span[class=v_b]").text(),
            doc.select("div[class=playlog_achievement_txt t_r]").text(),
            when (doc.select("img[class=h_35 m_5 f_l]")[0].attr("src")) {
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fc_dummy.png" -> ""
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fc.png" -> "FC"
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fcplus.png" -> "FC+"
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/ap.png" -> "AP"
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/applus.png" -> "AP+"
                else -> ""
            },
            when (doc.select("img[class=h_35 m_5 f_l]")[1].attr("src")) {
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fs_dummy.png" -> ""
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fs.png" -> "FS"
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fsplus.png" -> "FS+"
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fsd.png" -> "FSD"
                "https://maimai.wahlap.com/maimai-mobile/img/playlog/fsdplus.png" -> "FSD+"
                else -> ""
            },
            doc.select("div[class=white p_r_5 f_15 f_r]").text(),
            MaiObjectStats(
                "Tap",
                tapRow[0].text().toInt(),
                tapRow[1].text().toInt(),
                tapRow[2].text().toInt(),
                tapRow[3].text().toInt(),
                tapRow[4].text().toInt()
            ),
            MaiObjectStats(
                "Hold",
                holdRow[0].text().toInt(),
                holdRow[1].text().toInt(),
                holdRow[2].text().toInt(),
                holdRow[3].text().toInt(),
                holdRow[4].text().toInt()
            ),
            MaiObjectStats(
                "Slide",
                slideRow[0].text().toInt(),
                slideRow[1].text().toInt(),
                slideRow[2].text().toInt(),
                slideRow[3].text().toInt(),
                slideRow[4].text().toInt(),
            ),
            if (touchRow[1].text() == "") {
                null
            } else {
                MaiObjectStats(
                    "Touch",
                    touchRow[0].text().toInt(),
                    touchRow[1].text().toInt(),
                    touchRow[2].text().toInt(),
                    touchRow[3].text().toInt(),
                    touchRow[4].text().toInt(),
                )
            },
            MaiObjectStats(
                "Break",
                breakRow[0].text().toInt(),
                breakRow[1].text().toInt(),
                breakRow[2].text().toInt(),
                breakRow[3].text().toInt(),
                breakRow[4].text().toInt(),
            ),
            rating,
            baseRating,
            ratingChanges,
            doc.select("div[class=col2 f_l t_l f_0]").select("div[class=f_r f_14 white]").text(),
            doc.select("div[class=col2 p_l_5 f_l t_l f_0]").select("div[class=f_r f_14 white]").text(),
            matchingPlayers,
            doc.select("div[class=p_5 t_r]").select("form[class=d_ib v_t]").select("input[name=idx]").attr("value"),
            doc.select("div[class=p_5 t_r]").select("form[class=d_ib v_t]").select("input[name=diff]").attr("value")
        )
    }

    /**Get 50 recent plays from specific user. Returns a list contains 50 instances of MaiRecentTrack.
     *
     * Get [latestToken] after this action to retrieve the latest token.
     *
     * @param id A unique id for each player.
     * @param token A randomized token to validate each action.
     * @since NLBot2021 ver1.0
     */
    suspend fun getRecentRecord(id: String, token: String): MutableList<MaiRecentTrack> {
        val records = mutableListOf<MaiRecentTrack>()

        val returns = getResponseStringFromUrl(id, token, recentUrl)

        // println(returns[0])

        if (returns[1].isEmpty() || returns[2].isEmpty()) {
            throw Exception() // TODO: Throw a proper exception indicating wrong token or id
        }

        latestToken = returns[2]

        val doc = Jsoup.parse(returns[0])
        val recentList = doc.select("div[class=p_10 t_l f_0 v_b]")
        recentList.forEach {
            records.add(
                MaiRecentTrack(
                    it.select("input[name=idx]").attr("value"),
                    it.select("div[class=basic_block m_5 p_5 p_l_10 f_13 break]").text(),
                    it.select("span[class=red f_b v_b]").text(),
                    it.select("span[class=v_b]").text(),
                    it.select("div[class=playlog_achievement_txt t_r]").text(),
                    when (it.select("img[class=h_35 m_5 f_l]")[0].attr("src")) {
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fc_dummy.png" -> ""
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fc.png" -> "FC"
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fcplus.png" -> "FC+"
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/ap.png" -> "AP"
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/applus.png" -> "AP+"
                        else -> ""
                    },
                    when (it.select("img[class=h_35 m_5 f_l]")[1].attr("src")) {
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fs_dummy.png" -> ""
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fs.png" -> "FS"
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fsplus.png" -> "FS+"
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fsd.png" -> "FSD"
                        "https://maimai.wahlap.com/maimai-mobile/img/playlog/fsdplus.png" -> "FSD+"
                        else -> ""
                    },
                    it.select("div[class=white p_r_5 f_15 f_r]").text()
                )
            )
        }

        return records
    }

    /**Get response string and tokens from specific url. Returns all tokens needed to work with.
     *
     * @param id A unique id for each player.
     * @param token A randomized token to validate each action.
     * @param url Url to query.
     * @since NLBot2021 ver1.0
     */
    private suspend fun getResponseStringFromUrl(id: String, token: String, url: String): MutableList<String> {
        val returns = mutableListOf<String>()

        val client = HttpClient(CIO){
            Charsets {
                register(Charsets.UTF_8)
            }
        }

        val response: HttpResponse = client.get(url) {
            headers {
                cookie("_t", id)
                cookie("userId", token, httpOnly = true)
            }
        }

        response.setCookie().forEach {
            println(it.value)
        }

        val cookieList = response.setCookie()

        returns.add(response.readBytes().decodeToString())
        returns.add(cookieList[0].value)
        returns.add(cookieList[1].value)

        return returns
    }
}