import { httpClient } from "../../../shared/api/httpClient";
import type { ContentNode, ContentSection } from "../model/contentTypes";

export const contentApi = {
  async tree(section: ContentSection) {
    return (await httpClient.get<ContentNode[]>(`/v1/${section}/nodes`)).data;
  },

  async node(section: ContentSection, slug: string) {
    return (await httpClient.get<ContentNode>(`/v1/${section}/nodes/${slug}`)).data;
  },
};
