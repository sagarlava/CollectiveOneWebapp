<template lang="html">
  <div v-if="section"
    class="section-title-container w3-leftbar gray-1-border">

    <div class="w3-row title-row">
        <router-link tag="a" :to="{ name: 'ModelSection', params: { sectionId: section.id } }"
          class="section-title cursor-pointer w3-left" :style="sectionTitleStyle">
          {{ section.title }}
        </router-link>
        <div v-if="showMessagesIndicator" class="w3-left comments-indicator cursor-pointer"
          @click="$emit('show-messages')">
          <app-indicator
            :initiativeId="section.initiativeId"
            contextType="MODEL_SECTION"
            :contextElementId="section.id"
            :size="18"
            type="messages"
            :forceUpdate="forceUpdate">
          </app-indicator>
        </div>
    </div>

    <div class="also-in-container">
      <div v-if="floating" class="w3-row">
        <div class="w3-left">
          <i>in:</i>
        </div>
        <div v-for="parentSection in section.inSections" class="in-tag-container w3-left">
          <router-link :to="{ name: 'ModelSection', params: { sectionId: parentSection.id } }"
            class="gray-1 w3-tag w3-round w3-small">
            {{ parentSection.title }}
          </router-link>
        </div>
        <div v-for="parentView in section.inViews" class="in-tag-container w3-left">
          <router-link :to="{ name: 'ModelView', params: { viewId: parentView.id } }"
            class="gray-1 w3-tag w3-round w3-small">
            {{ parentView.title }}
          </router-link>
        </div>
      </div>
      <div v-if="!floating" class="w3-row w3-small also-in-row">
        <div v-if="sectionIsInOtherPlaces" class="">
          <div class="w3-left">
            <i>also in:</i>
          </div>
          <div v-for="parentSection in section.inSections" class="in-tag-container w3-left">
            <div v-if="parentSection.id !== inElementId" class="">
              <router-link :to="{ name: 'ModelSection', params: { sectionId: parentSection.id } }"
                class="gray-1 w3-tag w3-round w3-small">
                {{ parentSection.title }}
              </router-link>
            </div>
          </div>
          <div v-for="parentView in section.inViews" class="in-tag-container w3-left">
            <div v-if="parentView.id !== inElementId" class="">
              <router-link :to="{ name: 'ModelView', params: { viewId: parentView.id } }"
                class="gray-1 w3-tag w3-round w3-small">
                {{ parentView.title }}
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="expand" class="w3-row gray-1-border section-description w3-small">
      <vue-markdown class="marked-text" :source="section.description"></vue-markdown>
    </div>

  </div>
</template>

<script>
export default {
  props: {
    section: {
      type: Object,
      defaul: null
    },
    level: {
      type: Number,
      default: 0
    },
    floating: {
      type: Boolean,
      default: false
    },
    inElementId: {
      type: String,
      default: ''
    },
    expand: {
      type: Boolean,
      default: true
    },
    showMessagesIndicator: {
      type: Boolean,
      default: false
    },
    forceUpdate: {
      type: Boolean,
      default: false
    }
  },

  computed: {
    sectionTitleStyle () {
      var fontsize = this.level < 4 ? 22 - 4 * this.level : 12
      return {'font-size': fontsize + 'px'}
    },
    sectionIsInOtherPlaces () {
      var ix
      for (ix in this.section.inSections) {
        if (this.section.inSections[ix].id !== this.inElementId) {
          return true
        }
      }

      for (ix in this.section.inViews) {
        if (this.section.inViews[ix].id !== this.inElementId) {
          return true
        }
      }

      return false
    }
  }

}
</script>

<style scoped>

.section-title-container {
  border-top-width: 2px !important;
  padding-top: 8px;
  padding-left: 8px;
  padding-right: 8px;
  padding-bottom: 4px;
}

.title-row {
  margin-bottom: 4px;
}

.comments-indicator {
  padding-top: 0px;
  padding-left: 16px;
}

.also-in-row {
  margin-top: 8px;
}

.section-title:hover {
  color: #15a5cc;
}

.in-tag-container {
  display: inline-block;
  margin-left: 5px;
  margin-bottom: 5px;
}

</style>
