<template lang="html">
  <div class="submembers-table ">
    <table v-if="submembers.length > 0"
      class="w3-table w3-striped w3-bordered w3-centered table-element">
      <thead>
        <tr>
          <th class="avatar-col">AVATAR</th>
          <th>NICKNAME</th>
          <th v-for="asset in assets">{{ asset.assetName }}</th>
          <th class="subinitiatives-col">SUBINITIATIVES</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="submember in submembersSorted">
          <td class="avatar-col">
            <app-user-avatar :user="submember.user" :showName="false"></app-user-avatar>
          </td>
          <td>{{ submember.user.nickname }}</td>
          <td v-for="asset in assets">
            {{ ownedOfThisAsset(asset, submember) }}
          </td>
          <td>
            <router-link :to="{name: 'Initiative', params: {'initiativeId': subinitiative.id}}"
              v-for="subinitiative in submember.subinitiatives"
              :key="subinitiative.id"
              class="subinitiative-tag w3-tag w3-round-large cursor-pointer noselect"
              :style="{'background-color': $store.getters.colorOfInitiative(subinitiative.id)}">
              {{ subinitiative.name }}
            </router-link>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

</template>

<script>
import UserAvatar from '@/components/user/UserAvatar.vue'

export default {
  components: {
    'app-user-avatar': UserAvatar
  },

  props: {
    submembers: Array,
    assets: {
      type: Array,
      default: () => {
        return []
      }
    }
  },

  computed: {
    submembersSorted () {
      var subMembersTemp = JSON.parse(JSON.stringify(this.submembers))
      return subMembersTemp.sort((a, b) => { return b.receivedAssets[0].ownedByThisHolder - a.receivedAssets[0].ownedByThisHolder })
    }
  },

  methods: {
    ownedOfThisAsset (asset, submember) {
      for (var ix in submember.receivedAssets) {
        if (submember.receivedAssets[ix].assetId === asset.assetId) {
          return submember.receivedAssets[ix].ownedByThisHolder > 0 ? submember.receivedAssets[ix].ownedByThisHolder.toFixed(2) : '-'
        }
      }
      return '-'
    }
  }
}
</script>

<style scoped>

.submembers-table {
  max-height: 80vh;
  overflow-y: auto;
}

.avatar-col {
  /*width: 50px;*/
}

.subinitiative-tag {
  margin-left: 5px;
  margin-bottom: 3px;
}

</style>
