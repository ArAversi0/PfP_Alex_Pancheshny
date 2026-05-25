import { SiteLayout } from "../shared/ui/SiteLayout";

export function ContentPlaceholderPage({
  title,
  description,
}: {
  title: string;
  description: string;
}) {
  return (
    <SiteLayout>
      <p className="eyebrow">Archive section</p>
      <h1>{title}</h1>
      <p className="intro">{description}</p>
      <section className="empty-state">
        <p>This section is reserved for the next content pass.</p>
      </section>
    </SiteLayout>
  );
}
