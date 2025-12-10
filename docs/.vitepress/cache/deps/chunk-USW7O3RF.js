import {
  getConfig2
} from "./chunk-4DZFSGG3.js";
import {
  __name,
  select_default
} from "./chunk-QKEYNJIV.js";

// node_modules/.pnpm/mermaid@11.12.0/node_modules/mermaid/dist/chunks/mermaid.core/chunk-EXTU4WIE.mjs
var selectSvgElement = __name((id) => {
  const { securityLevel } = getConfig2();
  let root = select_default("body");
  if (securityLevel === "sandbox") {
    const sandboxElement = select_default(`#i${id}`);
    const doc = sandboxElement.node()?.contentDocument ?? document;
    root = select_default(doc.body);
  }
  const svg = root.select(`#${id}`);
  return svg;
}, "selectSvgElement");

export {
  selectSvgElement
};
//# sourceMappingURL=chunk-USW7O3RF.js.map
