export function downloadJson(filename: string, document: string): void {
  const blob = new Blob([document], { type: "application/json" });
  const url = URL.createObjectURL(blob);
  const link = window.document.createElement("a");
  link.href = url;
  link.download = `${filename.replace(/[^a-z0-9_-]+/gi, "-") || "character"}.json`;
  link.click();
  URL.revokeObjectURL(url);
}
