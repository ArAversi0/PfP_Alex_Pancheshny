import { httpClient } from "../../../shared/api/httpClient";
import type {
  AdminContentNode,
  AdminContentSection,
  AdminCharacterGroup,
  AdminDashboardSummary,
  AdminUser,
} from "../model/adminTypes";
import type { CharacterSheet } from "../../character-sheet/model/characterTypes";

const ADMIN_PATH = "/v1/admin";

export const adminApi = {
  async dashboard() {
    return (await httpClient.get<AdminDashboardSummary>(`${ADMIN_PATH}/dashboard`)).data;
  },

  async listContent(section: AdminContentSection) {
    return (await httpClient.get<AdminContentNode[]>(`${ADMIN_PATH}/content/nodes`, {
      params: { section },
    })).data;
  },

  async createContent(node: AdminContentNode) {
    return (await httpClient.post<AdminContentNode>(`${ADMIN_PATH}/content/nodes`, node)).data;
  },

  async updateContent(node: AdminContentNode) {
    return (
      await httpClient.put<AdminContentNode>(
        `${ADMIN_PATH}/content/nodes/${node.section}/${node.slug}`,
        node,
      )
    ).data;
  },

  async deleteContent(node: AdminContentNode) {
    await httpClient.delete(`${ADMIN_PATH}/content/nodes/${node.section}/${node.slug}`);
  },

  async listUsers() {
    return (await httpClient.get<AdminUser[]>(`${ADMIN_PATH}/users`)).data;
  },

  async deleteUser(userId: string) {
    await httpClient.delete(`${ADMIN_PATH}/users/${userId}`);
  },

  async listCharacters() {
    return (await httpClient.get<AdminCharacterGroup[]>(`${ADMIN_PATH}/characters`)).data;
  },

  async getCharacterSheet(characterId: string) {
    return (await httpClient.get<CharacterSheet>(`${ADMIN_PATH}/characters/${characterId}/sheet`)).data;
  },
};
