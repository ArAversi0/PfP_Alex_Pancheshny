export type AdminContentSection = "LORE" | "RULES";

export interface AdminDashboardSummary {
  users: number;
  characters: number;
  publishedContent: number;
}

export interface AdminContentNode {
  section: AdminContentSection;
  slug: string;
  parentSlug: string | null;
  title: string;
  summary: string;
  contentMarkdown: string;
  sortOrder: number;
  published: boolean;
}

export interface AdminUser {
  id: string;
  email: string;
  role: "ROLE_USER" | "ROLE_ADMIN";
  emailVerified: boolean;
  createdAt: string;
  characterCount: number;
}

export interface AdminCharacterCard {
  id: string;
  name: string;
  level: number;
  className: string;
  specialization: string;
  imageUrl: string;
}

export interface AdminCharacterGroup {
  userId: string;
  email: string;
  characters: AdminCharacterCard[];
}
