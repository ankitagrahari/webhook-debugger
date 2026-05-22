import { Link } from "react-router-dom";

/**
 * LegalFooter — drop this into LandingPage.jsx, LoginPage.jsx,
 * RegisterPage.jsx, and any other public-facing page.
 *
 * Usage:
 *   import LegalFooter from "../components/LegalFooter";
 *   ...
 *   <LegalFooter />
 */
export default function LegalFooter({ className = "" }) {
  return (
    <footer
      className={`border-t border-gray-800 bg-gray-950 px-6 py-8 text-center font-mono text-xs text-gray-500 ${className}`}
    >
      {/* Tagline */}
      <p className="text-gray-600 text-sm mb-4">
        HookSpy — Webhook Capture, Inspect &amp; Replay for Developers
      </p>

      {/* Legal navigation */}
      <nav
        aria-label="Legal links"
        className="flex flex-wrap gap-x-6 gap-y-2 justify-center mb-4"
      >
        <Link to="/terms" className="hover:text-accent-hover transition-colors duration-150">
          Terms &amp; Conditions
        </Link>
        <Link to="/privacy-policy" className="hover:text-accent-hover transition-colors duration-150">
          Privacy &amp; Cookies
        </Link>
        <Link to="/refund-policy" className="hover:text-accent-hover transition-colors duration-150">
          Refund &amp; Cancellation
        </Link>
        <a
          href="mailto:support.hookspy@gmail.com"
          className="hover:text-accent-hover transition-colors duration-150"
        >
        Support
        </a>
      </nav>

      {/* Copyright */}
      <p className="text-gray-700">
        © {new Date().getFullYear()} HookSpy. All rights reserved. &nbsp;·&nbsp; hookspy.in
      </p>
    </footer>
  );
}
