<template>
  <div>
    <div class="video-wrapper">
      <iframe :src="`https://www.youtube.com/embed/${this.selectedVideo.id.videoId}`"></iframe>
    </div>
    <p class="mt-3 subtitle">{{ selectedVideo.snippet.title }}</p>
  </div>
</template>

<script>
export default {
  props: {
    videos: { type: Array, default: () => [] },
    selectedVideo: { type: Object, default: () => ({}) }
  }
};
</script>

<style scoped>
.video-wrapper {
  position: relative;
  /* 16x9: 56.25% is (9/16)*100 */
  padding-top: 56.25%;
  height: 0;
  overflow: hidden;
  max-width: 100%;
  height: auto;
}
.video-wrapper iframe,
.video-wrapper object,
.video-wrapper embed {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
@import url("https://fonts.googleapis.com/css2?family=Jua&display=swap");
.subtitle {
  font-size: 13pt;
  font-family: "Jua", sans-serif;
  color: dimgray;
}
</style>