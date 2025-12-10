import {
  parse
} from "./chunk-UPXORBK5.js";
import "./chunk-NE5QXYRX.js";
import "./chunk-3RB2MZ42.js";
import "./chunk-XAQOV4AN.js";
import "./chunk-YU6O5BO4.js";
import "./chunk-AEFYVU4K.js";
import "./chunk-Y4OAM7UN.js";
import "./chunk-CSITAM5U.js";
import "./chunk-FLFPNCUT.js";
import "./chunk-MZBSQ3WT.js";
import {
  package_default
} from "./chunk-RQACMK24.js";
import {
  selectSvgElement
} from "./chunk-USW7O3RF.js";
import "./chunk-J7K2MAH4.js";
import {
  configureSvgSize
} from "./chunk-4DZFSGG3.js";
import "./chunk-GW2OTQRL.js";
import {
  __name,
  log
} from "./chunk-QKEYNJIV.js";
import "./chunk-IKZWERSR.js";

// node_modules/.pnpm/mermaid@11.12.0/node_modules/mermaid/dist/chunks/mermaid.core/infoDiagram-F6ZHWCRC.mjs
var parser = {
  parse: __name(async (input) => {
    const ast = await parse("info", input);
    log.debug(ast);
  }, "parse")
};
var DEFAULT_INFO_DB = {
  version: package_default.version + (true ? "" : "-tiny")
};
var getVersion = __name(() => DEFAULT_INFO_DB.version, "getVersion");
var db = {
  getVersion
};
var draw = __name((text, id, version) => {
  log.debug("rendering info diagram\n" + text);
  const svg = selectSvgElement(id);
  configureSvgSize(svg, 100, 400, true);
  const group = svg.append("g");
  group.append("text").attr("x", 100).attr("y", 40).attr("class", "version").attr("font-size", 32).style("text-anchor", "middle").text(`v${version}`);
}, "draw");
var renderer = { draw };
var diagram = {
  parser,
  db,
  renderer
};
export {
  diagram
};
//# sourceMappingURL=infoDiagram-F6ZHWCRC-2CZRJZNC.js.map
