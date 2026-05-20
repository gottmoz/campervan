import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import legacy from "@vitejs/plugin-legacy";

export default defineConfig({
  base: "./",
  plugins: [
    react(),
    legacy({
      targets: ["defaults", "Android >= 6"],
      modernPolyfills: true,
    }),
  ],
});
