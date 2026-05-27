export function FormMessage({
  kind,
  children,
}: {
  kind: "error" | "success" | "info";
  children: React.ReactNode;
}) {
  return <p className={`form-message ${kind}`}>{children}</p>;
}
