package com.luv2code.springboot.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.luv2code.springboot.demo.entity.InstagramDTO;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;


@RestController
@RequestMapping("/api/instagram")
public class InstagramController {

    private final RestTemplate rest;
    public static  String sessionId = "34080417699%3AzpbrU9OaVN9cgY%3A4%3AAYfU42mOecul2Dg8CIHzBRxfhP8dnmy66jHIqKaFDQ";

    public InstagramController(RestTemplateBuilder builder) {
        this.rest = builder.build();
    }
    private JsonNode fetchJson(String url) {
        RequestEntity<Void> req = RequestEntity.get(URI.create(url))
                .header("User-Agent",
                        "Instagram 123.1.0.26.115 (iPhone; CPU iPhone OS 14_0 like Mac OS X)")
                .header("Cookie", "sessionid=" + sessionId)
                .build();
        return rest.exchange(req, JsonNode.class).getBody();
    }

    @GetMapping("/profile/{user}")
    public InstagramDTO getProfile(@PathVariable String user) {

        JsonNode userNode = fetchProfileNode(user);
        InstagramDTO dto = new InstagramDTO();
        dto.setUsername(userNode.path("username").asText());
        dto.setFullName(userNode.path("full_name").asText());
        dto.setBiography(userNode.path("biography").asText());
        dto.setFollowers(userNode
                .path("edge_followed_by").path("count").asInt(0));
        dto.setFollowing(userNode
                .path("edge_follow").path("count").asInt(0));
        dto.setMediaCount(userNode
                .path("edge_owner_to_timeline_media").path("count").asInt(0));
        dto.setProfilePicUrl(userNode.path("profile_pic_url_hd").asText());

        JsonNode edges = userNode
                .path("edge_owner_to_timeline_media")
                .path("edges");
        if (edges.isArray() && edges.size() > 0) {
            dto.setLatestMediaUrl(
                    edges.get(0).path("node").path("display_url").asText()
            );
        }
        return dto;
    }

    @GetMapping("/profile/{user}/followers")
    public List<String> getRecentFollowers(@PathVariable String user) {
        JsonNode userNode = fetchProfileNode(user);
        long userId = userNode.path("id").asLong();
        String url = String.format(
                "https://i.instagram.com/api/v1/friendships/%d/followers/?count=20",
                userId
        );
        JsonNode body = fetchJson(url);
        JsonNode users = body.path("users");
        return StreamSupport.stream(users.spliterator(), false)
                .map(n -> n.path("username").asText())
                .toList();
    }




    @GetMapping("/profile/{user}/following")
    public List<String> getRecentFollowing(@PathVariable String user) {
        JsonNode userNode = fetchProfileNode(user);
        long userId = userNode.path("id").asLong();
        String url = String.format(
                "https://i.instagram.com/api/v1/friendships/%d/following/?count=20",
                userId
        );
        JsonNode body = fetchJson(url);
        JsonNode users = body.path("users");
        return StreamSupport.stream(users.spliterator(), false)
                .map(n -> n.path("username").asText())
                .toList();
    }

    private JsonNode fetchProfileNode(String user) {
        String url = "https://i.instagram.com/api/v1/users/web_profile_info/?username=" + user;
        RequestEntity<Void> req = RequestEntity.get(URI.create(url))
                .header("User-Agent",
                        "Instagram 123.1.0.26.115 (iPhone; CPU iPhone OS 14_0 like Mac OS X)")
                .header("X-IG-App-ID", "936619743392459")
                .header("Cookie", "sessionid=" + sessionId)
                .build();

        ResponseEntity<JsonNode> resp = rest.exchange(req, JsonNode.class);
        JsonNode userNode = resp.getBody()
                .path("data")
                .path("user");

        if (userNode.isMissingNode()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found or unavailable"
            );
        }
        return userNode;
    }
    @GetMapping("/profile/{user}/stories")
    public ArrayNode getStories(@PathVariable String user) throws Exception {
        final String MOBILE_UA  =
                "Instagram 123.1.0.26.115 (iPhone; CPU iPhone OS 14_0 like Mac OS X)";
        final String SYNC_URL   = "https://i.instagram.com/api/v1/qe/sync/";
        final String TRAY_URL   = "https://i.instagram.com/api/v1/feed/reels_tray/";
        final String MEDIA_TPL  = "https://i.instagram.com/api/v1/feed/user/%s/reel_media/";
        final String IG_APP_ID  = "936619743392459";

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode result    = mapper.createArrayNode();
        String wwwClaim = "";
        try {
            ResponseEntity<Void> syncResp = rest.exchange(
                    RequestEntity.get(URI.create(SYNC_URL))
                            .header("User-Agent", MOBILE_UA)
                            .header("Cookie",    "sessionid=" + sessionId)
                            .header("X-IG-App-ID", IG_APP_ID)
                            .build(),
                    Void.class
            );
            wwwClaim = syncResp.getHeaders().getFirst("X-IG-WWW-Claim");
        } catch (HttpClientErrorException ignored) { }
        JsonNode tray = fetchJsonWithHeaders(TRAY_URL, MOBILE_UA, IG_APP_ID, wwwClaim)
                .path("tray");
        JsonNode reel = StreamSupport.stream(tray.spliterator(), false)
                .filter(n -> user.equals(n.path("user").path("username").asText()))
                .findFirst().orElse(null);

        if (reel == null || reel.path("user").path("is_private").asBoolean(false)) {
            return result;
        }

        String reelId = reel.path("user").path("pk").asText();
        Thread.sleep(3000);
        String mediaUrl = String.format(MEDIA_TPL, reelId);
        JsonNode items = fetchJsonWithHeaders(mediaUrl, MOBILE_UA, IG_APP_ID, wwwClaim)
                .path("items");
        if (!items.isArray() || items.isEmpty()) {
            return result;
        }
        Files.createDirectories(Paths.get("stories", user));
        for (JsonNode item : items) {
            boolean isVideo = item.has("video_versions");
            JsonNode version = isVideo
                    ? item.path("video_versions").get(0)
                    : item.path("image_versions2").path("candidates").get(0);

            String url      = version.path("url").asText();
            String ext      = isVideo ? ".mp4" : ".jpg";
            String rawName  = Paths.get(new URL(url).getPath()).getFileName().toString();
            String filename = rawName.endsWith(ext) ? rawName : UUID.randomUUID() + ext;

            try (InputStream in = new URL(url).openStream();
                 FileOutputStream out = new FileOutputStream(
                         Paths.get("stories", user, filename).toFile())) {
                StreamUtils.copy(in, out);
            }

            ObjectNode node = result.addObject();
            node.put("type", isVideo ? "video" : "image");
            node.put("url", url);
            node.put("filename", filename);
        }

        return result;
    }

    private JsonNode fetchJsonWithHeaders(
            String url, String ua, String igAppId, String wwwClaim
    ) {
        RequestEntity<Void> req = RequestEntity.get(URI.create(url))
                .header("User-Agent",         ua)
                .header("Cookie",             "sessionid=" + sessionId)
                .header("X-IG-App-ID",        igAppId)
                .header("X-Requested-With",   "XMLHttpRequest")
                .header("X-IG-WWW-Claim",     wwwClaim != null ? wwwClaim : "")
                .build();

        return rest.exchange(req, JsonNode.class).getBody();
    }


}
