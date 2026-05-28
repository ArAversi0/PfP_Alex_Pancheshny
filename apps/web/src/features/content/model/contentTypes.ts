export type ContentSection = "lore" | "rules";

export interface ContentNode {
  slug: string;
  parentSlug: string | null;
  title: string;
  summary: string;
  contentMarkdown: string;
  sortOrder: number;
  children: ContentNode[];
}
