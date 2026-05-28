import { useEffect, useMemo, useState, type ReactNode } from "react";
import { Link, useParams } from "react-router-dom";
import { getApiErrorMessage } from "../../auth/api/authApi";
import { FormMessage } from "../../auth/components/FormMessage";
import { contentApi } from "../api/contentApi";
import type { ContentNode, ContentSection } from "../model/contentTypes";
import { SiteLayout } from "../../../shared/ui/SiteLayout";

interface ContentBrowserPageProps {
  section: ContentSection;
  title: string;
  description: string;
}

export function ContentBrowserPage({ section, title, description }: ContentBrowserPageProps) {
  const { slug } = useParams();
  const [tree, setTree] = useState<ContentNode[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    setLoading(true);
    setError("");
    contentApi.tree(section).then(
      (loaded) => setTree(loaded),
      (loadError: unknown) => setError(getApiErrorMessage(loadError)),
    ).finally(() => setLoading(false));
  }, [section]);

  const selectedNode = useMemo(() => {
    if (tree.length === 0) {
      return null;
    }
    return (slug ? findNode(tree, slug) : null) ?? tree[0];
  }, [slug, tree]);

  return (
    <SiteLayout wide>
      <div className="page-heading">
        <p className="eyebrow">{section === "lore" ? "World archive" : "Reference archive"}</p>
        <h1>{title}</h1>
        <p className="intro">{description}</p>
      </div>

      {error && <FormMessage kind="error">{error}</FormMessage>}
      {loading ? (
        <p className="loading-copy">Opening archive...</p>
      ) : selectedNode ? (
        <section className="content-browser">
          <aside className="content-sidebar" aria-label={`${title} navigation`}>
            <p className="eyebrow">Contents</p>
            <nav className="content-tree">
              {tree.map((node) => (
                <ContentTreeLink
                  key={node.slug}
                  node={node}
                  section={section}
                  selectedSlug={selectedNode.slug}
                />
              ))}
            </nav>
          </aside>
          <article className="content-article">
            <p className="eyebrow">{selectedNode.children.length > 0 ? "Category" : "Article"}</p>
            <h2>{selectedNode.title}</h2>
            {selectedNode.summary && <p className="content-summary">{selectedNode.summary}</p>}
            {selectedNode.contentMarkdown.trim() ? (
              <MarkdownContent markdown={selectedNode.contentMarkdown} />
            ) : (
              <p className="empty-copy">This article is waiting for content.</p>
            )}
            {selectedNode.children.length > 0 && (
              <section className="content-child-list" aria-label="Articles in this category">
                <p className="eyebrow">In this category</p>
                <div>
                  {selectedNode.children.map((child) => (
                    <Link key={child.slug} to={`/${section}/${child.slug}`}>
                      <strong>{child.title}</strong>
                      {child.summary && <span>{child.summary}</span>}
                    </Link>
                  ))}
                </div>
              </section>
            )}
          </article>
        </section>
      ) : (
        <section className="empty-state">
          <p className="eyebrow">Empty archive</p>
          <h2>No articles yet.</h2>
          <p>The structure is ready for the first content pass.</p>
        </section>
      )}
    </SiteLayout>
  );
}

function ContentTreeLink({
  node,
  section,
  selectedSlug,
}: {
  node: ContentNode;
  section: ContentSection;
  selectedSlug: string;
}) {
  const active = node.slug === selectedSlug;
  return (
    <div className="content-tree-node">
      <Link className={active ? "active" : undefined} to={`/${section}/${node.slug}`}>
        {node.title}
      </Link>
      {node.children.length > 0 && (
        <div className="content-tree-children">
          {node.children.map((child) => (
            <ContentTreeLink
              key={child.slug}
              node={child}
              section={section}
              selectedSlug={selectedSlug}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function MarkdownContent({ markdown }: { markdown: string }) {
  return <div className="markdown-content">{renderMarkdown(markdown)}</div>;
}

function renderMarkdown(markdown: string): ReactNode[] {
  const lines = markdown.replace(/\r\n/g, "\n").split("\n");
  const nodes: ReactNode[] = [];
  let index = 0;

  while (index < lines.length) {
    const line = lines[index];
    if (!line.trim()) {
      index += 1;
      continue;
    }

    const heading = /^(#{1,3})\s+(.+)$/.exec(line);
    if (heading) {
      const level = heading[1].length;
      const content = renderInline(heading[2], `heading-${index}`);
      nodes.push(level === 1
        ? <h3 key={index}>{content}</h3>
        : level === 2
          ? <h4 key={index}>{content}</h4>
          : <h5 key={index}>{content}</h5>);
      index += 1;
      continue;
    }

    if (isListLine(line)) {
      const items: ReactNode[] = [];
      const ordered = isOrderedListLine(line);
      while (index < lines.length) {
        if (isListLine(lines[index]) && isOrderedListLine(lines[index]) === ordered) {
          items.push(<li key={index}>{renderInline(stripListMarker(lines[index]), `list-${index}`)}</li>);
          index += 1;
          continue;
        }
        if (!lines[index].trim() && nextNonEmptyListLine(lines, index, ordered)) {
          index += 1;
          continue;
        }
        break;
      }
      nodes.push(ordered ? <ol key={`list-${index}`}>{items}</ol> : <ul key={`list-${index}`}>{items}</ul>);
      continue;
    }

    const paragraph: string[] = [];
    while (index < lines.length && lines[index].trim() && !/^(#{1,3})\s+/.test(lines[index]) && !isListLine(lines[index])) {
      paragraph.push(lines[index].trim());
      index += 1;
    }
    nodes.push(<p key={index}>{renderInline(paragraph.join(" "), `paragraph-${index}`)}</p>);
  }

  return nodes;
}

function renderInline(text: string, keyPrefix: string): ReactNode[] {
  return text.split(/(\*\*[^*]+\*\*)/g).filter(Boolean).map((part, index) => {
    if (part.startsWith("**") && part.endsWith("**")) {
      return <strong key={`${keyPrefix}-${index}`}>{part.slice(2, -2)}</strong>;
    }
    return part;
  });
}

function isListLine(line: string) {
  return /^\s*(?:[-*]|\d+[).])\s+/.test(line);
}

function isOrderedListLine(line: string) {
  return /^\s*\d+[).]\s+/.test(line);
}

function nextNonEmptyListLine(lines: string[], fromIndex: number, ordered: boolean) {
  let index = fromIndex + 1;
  while (index < lines.length && !lines[index].trim()) {
    index += 1;
  }
  return index < lines.length && isListLine(lines[index]) && isOrderedListLine(lines[index]) === ordered;
}

function stripListMarker(line: string) {
  return line.replace(/^\s*(?:[-*]|\d+[).])\s+/, "");
}

function findNode(nodes: ContentNode[], slug: string): ContentNode | null {
  for (const node of nodes) {
    if (node.slug === slug) {
      return node;
    }
    const child = findNode(node.children, slug);
    if (child) {
      return child;
    }
  }
  return null;
}
