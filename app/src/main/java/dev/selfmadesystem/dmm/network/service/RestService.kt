package dev.selfmadesystem.dmm.network.service

import dev.selfmadesystem.dmm.domain.manager.PreferenceManager
import dev.selfmadesystem.dmm.network.dto.Commit
import dev.selfmadesystem.dmm.network.dto.Index
import dev.selfmadesystem.dmm.network.dto.Release
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RestService(
    private val httpService: HttpService,
    private val prefs: PreferenceManager
) {

    suspend fun getLatestRelease(repo: String) = withContext(Dispatchers.IO) {
        httpService.request<Release> {
            // TODO: Rename to use Discord Mod Manager instead
            url("https://api.github.com/repos/vendetta-mod/$repo/releases/latest")
        }
    }

    suspend fun getLatestDiscordVersions() = withContext(Dispatchers.IO) {
        httpService.request<Index> {
            url("${prefs.mirror.baseUrl}/tracker/index")
        }
    }

    suspend fun getCommits(repo: String, page: Int = 1) = withContext(Dispatchers.IO) {
        httpService.request<List<Commit>> {
            // TODO: Rename to use Discord Mod Manager instead
            url("https://api.github.com/repos/vendetta-mod/$repo/commits")
            parameter("page", page)
        }
    }

}