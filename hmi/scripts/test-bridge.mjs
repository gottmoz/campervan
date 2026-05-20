import assert from "node:assert/strict";

globalThis.window = {};
const { camperAgentBridge } = await import("../src/integrations/camperAgentBridge.js");

const fallback = await camperAgentBridge.getIntegrationSnapshot();
assert.equal(fallback.ok, true);
assert.equal(fallback.data.readOnly, true);

globalThis.window.CamperAgent = {
  getIntegrationSnapshot: () => "{not-json",
};
const invalid = await camperAgentBridge.getIntegrationSnapshot();
assert.equal(invalid.ok, false);

console.log("camperAgentBridge fallback tests passed");
