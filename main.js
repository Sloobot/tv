document.addEventListener("DOMContentLoaded", function() {
  var videoPlayer = document.getElementById("videoPlayer");
  videoPlayer.requestFullscreen();

  var hls;
  var offlinePopup = document.getElementById("offline-popup");

  if(Hls.isSupported()) {
    hls = new Hls();
    hls.loadSource("https://janus.xpbroadcasting.com:8443/hls/xptv1.m3u8");
    hls.attachMedia(videoPlayer);
    hls.on(Hls.Events.ERROR, function(event, data) {
      if (data.fatal) {
        switch(data.type) {
          case Hls.ErrorTypes.NETWORK_ERROR:
            console.error("fatal network error encountered, try to recover");
            offlinePopup.style.display = "block";
            hls.startLoad();
            break;
          case Hls.ErrorTypes.MEDIA_ERROR:
            console.error("fatal media error encountered, try to recover");
            offlinePopup.style.display = "block";
            hls.recoverMediaError();
            break;
          default:
            // cannot recover
            hls.destroy();
            break;
        }
      }
    });
  } else if (videoPlayer.canPlayType("application/vnd.apple.mpegurl")) {
    videoPlayer.src = "https://janus.xpbroadcasting.com:8443/hls/xptv1.m3u8";
    videoPlayer.addEventListener("loadedmetadata", function() {
      videoPlayer.play();
    });
  }

  videoPlayer.play();
  
  document.addEventListener("keydown", function(event) {
    switch (event.keyCode) {
      case 10009: // Return key
        window.history.back();
        break;
      case 10252: // Play key
        if (videoPlayer.paused) {
          videoPlayer.play();
        } else {
          videoPlayer.pause();
        }
        break;
      case 414: // Stop key
        videoPlayer.pause();
        videoPlayer.currentTime = 0;
        break;
    }
  });

  window.addEventListener("online", function() {
    offlinePopup.style.display = "none";
    if (hls) {
      hls.startLoad();
    }
  });

  window.addEventListener("offline", function() {
    offlinePopup.style.display = "block";
  });
});
