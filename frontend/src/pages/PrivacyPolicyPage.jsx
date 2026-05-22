import { Link } from "react-router-dom";

const Section = ({ id, label, children }) => (
  <section id={id} className="mb-10">
    <h2 className="font-mono text-xs font-semibold text-accent tracking-widest uppercase mb-3 pb-2 border-b border-gray-800">
      {label}
    </h2>
    {children}
  </section>
);

const Table = ({ headers, rows }) => (
  <div className="overflow-x-auto my-4">
    <table className="w-full text-sm border-collapse">
      <thead>
        <tr>
          {headers.map((h) => (
            <th
              key={h}
              className="font-mono text-xs text-accent uppercase tracking-widest text-left px-3 py-2 border border-gray-800 bg-gray-900"
            >
              {h}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row, i) => (
          <tr key={i} className={i % 2 === 1 ? "bg-white/[0.02]" : ""}>
            {row.map((cell, j) => (
              <td key={j} className="text-gray-400 px-3 py-2 border border-gray-800 align-top">
                {cell}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  </div>
);

export default function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">
      {/* Header */}
      <header className="sticky top-0 z-50 border-b border-gray-800 bg-gray-950/90 backdrop-blur-md px-6 py-4 flex items-center gap-3">
        <Link to="/" className="font-mono font-bold text-accent text-lg">
          Hook<span className="text-gray-100">Spy</span>
        </Link>
        <span className="font-mono text-xs font-semibold bg-teal-400/10 text-accent border border-teal-400/30 px-2 py-0.5 rounded tracking-widest uppercase">
          Legal
        </span>
      </header>

      <main className="max-w-3xl mx-auto px-6 py-12 pb-20">
        <h1 className="font-mono text-3xl font-bold text-gray-100 mb-1">
          <span className="text-accent">Privacy &amp; Cookies Policy</span>
        </h1>
        <div className="flex flex-wrap gap-4 font-mono text-xs text-gray-500 mb-10">
          <span>Effective: 23 May 2025</span>
          <span>Last Updated: 23 May 2025</span>
          <span>hookspy.in</span>
        </div>

        <div className="bg-sky-950/30 border border-sky-500/30 border-l-2 border-l-sky-400 rounded-r-md p-4 mb-10 text-sm text-sky-200">
          This policy explains how HookSpy collects, uses, stores, and protects your personal data.
          We are committed to compliance with India's DPDP Act 2023 and applicable international
          standards including GDPR.
        </div>

        <Section id="controller" label="01 — Data Controller">
          <p className="text-gray-400 text-sm leading-relaxed">
            HookSpy operates hookspy.in and is the Data Fiduciary/Controller for all personal data
            processed under this policy.{" "}
            Contact:{" "}
            <a href="mailto:support.hookspy@gmail.com" className="text-accent hover:underline">
              support.hookspy@gmail.com
            </a>
          </p>
        </Section>

        <Section id="collected" label="02 — What Data We Collect">
          <h3 className="font-mono text-xs text-sky-400 font-semibold uppercase tracking-wide mb-2 mt-4">
            Account &amp; Identity Data
          </h3>
          <ul className="list-disc list-inside space-y-1 text-gray-400 text-sm mb-4">
            <li>Name, email address, password (hashed with bcrypt)</li>
            <li>Organisation/company name (optional)</li>
            <li>Billing address and GST number (for invoicing)</li>
          </ul>

          <h3 className="font-mono text-xs text-sky-400 font-semibold uppercase tracking-wide mb-2">
            Usage &amp; Technical Data
          </h3>
          <ul className="list-disc list-inside space-y-1 text-gray-400 text-sm mb-4">
            <li>IP address, browser type, operating system</li>
            <li>Pages visited, features used, session duration</li>
            <li>API access logs and error logs</li>
          </ul>

          <h3 className="font-mono text-xs text-sky-400 font-semibold uppercase tracking-wide mb-2">
            Webhook Payload Data
          </h3>
          <ul className="list-disc list-inside space-y-1 text-gray-400 text-sm mb-4">
            <li>HTTP headers and body of webhook requests routed to your HookSpy endpoint</li>
            <li>Request timestamps, source IPs, and response codes</li>
            <li>
              <strong className="text-gray-300">Note:</strong> You are solely responsible for ensuring
              no unlawfully obtained personal data is routed through HookSpy endpoints.
            </li>
          </ul>

          <h3 className="font-mono text-xs text-sky-400 font-semibold uppercase tracking-wide mb-2">
            Payment Data
          </h3>
          <p className="text-gray-400 text-sm">
            We do not store card numbers or bank details. Payment processing is handled entirely by
            Razorpay. We receive only transaction confirmation and masked payment identifiers.
          </p>
        </Section>

        <Section id="usage" label="03 — How We Use Your Data">
          <Table
            headers={["Purpose", "Legal Basis"]}
            rows={[
              ["Providing and operating the Service", "Contract performance"],
              ["Processing payments and issuing invoices", "Contract performance / Legal obligation"],
              ["Sending transactional emails (account, billing)", "Contract performance"],
              ["Security monitoring and fraud prevention", "Legitimate interest"],
              ["Product improvement and analytics", "Legitimate interest"],
              ["Marketing emails (opt-in only)", "Consent"],
              ["Legal compliance and law enforcement requests", "Legal obligation"],
            ]}
          />
        </Section>

        <Section id="retention" label="04 — Data Retention">
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm">
            <li><strong className="text-gray-300">Webhook payloads:</strong> Free — 48 hours · Pro — 30 days · Team — 90 days</li>
            <li><strong className="text-gray-300">Account data:</strong> Duration of subscription + 90 days after account closure</li>
            <li><strong className="text-gray-300">Billing records:</strong> 7 years (Indian taxation law)</li>
            <li><strong className="text-gray-300">Logs:</strong> 30-day rolling window</li>
          </ul>
        </Section>

        <Section id="sharing" label="05 — Data Sharing">
          <p className="text-gray-400 text-sm mb-3">We do not sell your personal data. We may share data with:</p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm">
            <li><strong className="text-gray-300">Razorpay</strong> — for payment processing</li>
            <li><strong className="text-gray-300">Cloud infrastructure providers</strong> (AWS/GCP) — for hosting and storage</li>
            <li><strong className="text-gray-300">Analytics services</strong> — anonymised usage data only</li>
            <li><strong className="text-gray-300">Legal authorities</strong> — when required by court order or applicable law</li>
          </ul>
        </Section>

        <Section id="rights" label="06 — Your Rights">
          <p className="text-gray-400 text-sm mb-3">Under the DPDP Act 2023 and applicable law, you have the right to:</p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm">
            <li><strong className="text-gray-300">Access</strong> — Request a copy of your personal data</li>
            <li><strong className="text-gray-300">Correction</strong> — Request correction of inaccurate data</li>
            <li><strong className="text-gray-300">Erasure</strong> — Request deletion of your account and associated data</li>
            <li><strong className="text-gray-300">Portability</strong> — Receive your data in a machine-readable format</li>
            <li><strong className="text-gray-300">Withdraw Consent</strong> — Opt out of marketing communications at any time</li>
            <li><strong className="text-gray-300">Grievance Redressal</strong> — File a complaint via <a href="mailto:support.hookspy@gmail.com" className="text-accent hover:underline">support.hookspy@gmail.com</a></li>
          </ul>
          <p className="text-gray-400 text-sm mt-3">We will respond to data requests within 30 days.</p>
        </Section>

        <Section id="cookies" label="07 — Cookies Policy">
          <p className="text-gray-400 text-sm mb-4">
            Cookies are small text files stored in your browser to enable site functionality,
            remember preferences, and collect analytics data.
          </p>
          <Table
            headers={["Cookie", "Type", "Purpose", "Duration"]}
            rows={[
              ["hs_session", "Essential", "Authentication and session management", "Session"],
              ["hs_csrf", "Essential", "Cross-site request forgery protection", "Session"],
              ["hs_prefs", "Functional", "User UI preferences (theme, layout)", "1 year"],
              ["_hs_analytics", "Analytics", "Usage analytics (anonymised)", "90 days"],
            ]}
          />
          <p className="text-gray-400 text-sm">
            You can manage cookies through your browser settings. Disabling essential cookies will
            prevent login functionality. Analytics cookies can be disabled without affecting platform
            use.
          </p>
        </Section>

        <Section id="security" label="08 — Security">
          <p className="text-gray-400 text-sm leading-relaxed">
            We implement TLS encryption in transit, AES-256 encryption at rest, access control and
            role-based permissions, regular security audits, and Kafka-backed audit logging for all
            data access events. In the event of a data breach, we will notify affected users within
            72 hours as required under applicable law.
          </p>
        </Section>

        <Section id="children" label="09 — Children's Privacy">
          <p className="text-gray-400 text-sm leading-relaxed">
            HookSpy is not intended for users under the age of 18. We do not knowingly collect data
            from minors. Contact{" "}
            <a href="mailto:support.hookspy@gmail.com" className="text-accent hover:underline">
              support.hookspy@gmail.com
            </a>{" "}
            if you believe a minor has created an account.
          </p>
        </Section>

        <Section id="changes" label="10 — Changes to This Policy">
          <p className="text-gray-400 text-sm leading-relaxed">
            We may update this Privacy Policy periodically. Material changes will be communicated
            via email or a prominent notice on the platform at least 7 days before taking effect.
          </p>
        </Section>

        <Section id="contact" label="11 — Contact &amp; Grievance Officer">
          <p className="text-gray-400 text-sm leading-relaxed">
            For privacy-related queries, data requests, or grievances:{" "}
            <a href="mailto:support.hookspy@gmail.com" className="text-accent hover:underline">
              support.hookspy@gmail.com
            </a>
            <br />
            Response time: within 30 days · Jurisdiction: Gurugram, Haryana, India
          </p>
        </Section>
      </main>

      <LegalFooter />
    </div>
  );
}

function LegalFooter() {
  return (
    <footer className="border-t border-gray-800 px-6 py-8 text-center font-mono text-xs text-gray-500">
      <nav className="flex flex-wrap gap-x-6 gap-y-2 justify-center mb-3">
        <Link to="/terms" className="hover:text-accent transition-colors">Terms &amp; Conditions</Link>
        <Link to="/privacy-policy" className="hover:text-accent transition-colors">Privacy &amp; Cookies</Link>
        <Link to="/refund-policy" className="hover:text-accent transition-colors">Refund &amp; Cancellation</Link>
        <a href="mailto:support.hookspy@gmail.com" className="hover:text-accent transition-colors">Support</a>
      </nav>
      <p className="text-gray-600">© 2025 HookSpy. All rights reserved.</p>
    </footer>
  );
}
