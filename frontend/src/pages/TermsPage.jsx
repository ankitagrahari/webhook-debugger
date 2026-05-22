import { Link } from "react-router-dom";

const Section = ({ id, label, children }) => (
  <section id={id} className="mb-10">
    <h2 className="font-mono text-xs font-semibold text-accent tracking-widest uppercase mb-3 pb-2 border-b border-gray-800">
      {label}
    </h2>
    {children}
  </section>
);

const BanBox = ({ children }) => (
  <div className="bg-red-950/30 border border-red-500/30 border-l-2 border-l-red-500 rounded-r-md p-4 my-4">
    <p className="text-red-300 text-sm">{children}</p>
  </div>
);

export default function TermsPage() {
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
          <span className="text-accent">Terms &amp; Conditions</span>
        </h1>
        <div className="flex flex-wrap gap-4 font-mono text-xs text-gray-500 mb-10">
          <span>Effective: 23 May 2025</span>
          <span>Last Updated: 23 May 2025</span>
          <span>hookspy.in</span>
        </div>

        {/* Warning banner */}
        <div className="bg-amber-950/30 border border-amber-500/30 border-l-2 border-l-amber-400 rounded-r-md p-4 mb-10 text-sm text-amber-200">
          <strong className="block mb-1">⚠️ Important — Read Before Using HookSpy</strong>
          HookSpy is a developer tool for legitimate webhook debugging and testing only. Malicious
          use, proxying illegal traffic, or facilitating fraud will result in immediate and permanent
          account termination without refund.
        </div>

        <Section id="acceptance" label="01 — Acceptance of Terms">
          <p className="text-gray-400 text-sm leading-relaxed mb-3">
            By accessing or using HookSpy ("Service", "Platform") at hookspy.in, you ("User", "you")
            agree to be bound by these Terms & Conditions and all applicable laws. If you do not
            agree, you must not use the Service.
          </p>
          <p className="text-gray-400 text-sm leading-relaxed">
            These Terms constitute a legally binding agreement between you and HookSpy. We may update
            these Terms at any time; continued use after changes constitutes acceptance.
          </p>
        </Section>

        <Section id="eligibility" label="02 — Eligibility">
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm">
            <li>You must be at least 18 years old to use this Service.</li>
            <li>You must have the legal capacity to enter into binding contracts in your jurisdiction.</li>
            <li>You must not be prohibited from using the Service under applicable laws.</li>
          </ul>
        </Section>

        <Section id="permitted" label="03 — Permitted Use">
          <p className="text-gray-400 text-sm mb-3">HookSpy is designed exclusively for:</p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm">
            <li>Inspecting, testing, and debugging webhook payloads from legitimate services (Stripe, Razorpay, GitHub, etc.)</li>
            <li>Replaying captured webhook events during development and QA cycles</li>
            <li>Monitoring webhook delivery and diagnosing integration failures</li>
            <li>Building and testing webhook-based integrations in non-production environments</li>
          </ul>
        </Section>

        <Section id="prohibited" label="04 — Prohibited Conduct">
          <p className="text-gray-400 text-sm mb-3">You must NOT use HookSpy to:</p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm mb-4">
            <li>Transmit, receive, or inspect traffic that facilitates fraud, phishing, money laundering, or any criminal activity</li>
            <li>Proxy, route, or relay malicious payloads through our infrastructure</li>
            <li>Intercept webhook traffic belonging to a third party without their explicit written consent</li>
            <li>Conduct or facilitate DDoS attacks, spam operations, or bot traffic</li>
            <li>Attempt to reverse-engineer, decompile, or exploit the platform's infrastructure</li>
            <li>Violate any applicable law, regulation, or third-party rights</li>
            <li>Resell or sublicense access to the Service without prior written authorisation</li>
          </ul>
          <BanBox>
            <strong className="text-red-400">⛔ Immediate Ban Policy: </strong>
            Any account found engaging in malicious activity, proxying illegal or fraudulent traffic,
            or attempting to exploit or harm other users or third-party services will be{" "}
            <strong className="text-red-400">immediately and permanently suspended</strong> without
            prior notice and without any refund. We reserve the right to report such activity to
            relevant law enforcement, including CERT-In (India).
          </BanBox>
        </Section>

        <Section id="account" label="05 — Account &amp; Security">
          <p className="text-gray-400 text-sm leading-relaxed">
            You are responsible for maintaining the confidentiality of your account credentials.
            Notify us immediately at{" "}
            <a href="mailto:security@hookspy.in" className="text-accent hover:underline">
              security@hookspy.in
            </a>{" "}
            if you suspect unauthorised access. We are not liable for losses resulting from
            compromised credentials due to your negligence.
          </p>
        </Section>

        <Section id="payments" label="06 — Subscription &amp; Payments">
          <p className="text-gray-400 text-sm leading-relaxed">
            HookSpy offers subscription-based plans billed monthly or annually. Payments are
            processed by Razorpay. All prices are in Indian Rupees (INR) inclusive of applicable
            GST. Subscription and refund terms are governed by our{" "}
            <Link to="/refund-policy" className="text-accent hover:underline">
              Refund &amp; Cancellation Policy
            </Link>.
          </p>
        </Section>

        <Section id="data" label="07 — Data &amp; Privacy">
          <p className="text-gray-400 text-sm leading-relaxed">
            Webhook payloads captured via HookSpy may contain sensitive data. You are solely
            responsible for ensuring that capturing such data complies with applicable data
            protection laws (including India's DPDP Act 2023). Refer to our{" "}
            <Link to="/privacy-policy" className="text-accent hover:underline">
              Privacy Policy
            </Link>{" "}
            for full details.
          </p>
        </Section>

        <Section id="ip" label="08 — Intellectual Property">
          <p className="text-gray-400 text-sm leading-relaxed">
            All platform code, UI, branding, and documentation are the exclusive property of HookSpy
            and protected under applicable IP laws. You are granted a limited, non-exclusive,
            non-transferable licence to use the Service during your subscription period.
          </p>
        </Section>

        <Section id="liability" label="09 — Limitation of Liability">
          <p className="text-gray-400 text-sm leading-relaxed">
            To the maximum extent permitted by law, HookSpy shall not be liable for any indirect,
            incidental, special, consequential, or punitive damages, including loss of data, revenue,
            or profits. Our total aggregate liability shall not exceed the amount paid by you in the
            3 months preceding the claim.
          </p>
        </Section>

        <Section id="termination" label="10 — Termination">
          <p className="text-gray-400 text-sm leading-relaxed">
            We may terminate or suspend your account at our sole discretion for violations of these
            Terms, non-payment, or any activity we deem harmful. You may cancel your subscription at
            any time; cancellation terms are in the{" "}
            <Link to="/refund-policy" className="text-accent hover:underline">
              Refund &amp; Cancellation Policy
            </Link>.
          </p>
        </Section>

        <Section id="law" label="11 — Governing Law &amp; Disputes">
          <p className="text-gray-400 text-sm leading-relaxed">
            These Terms are governed by the laws of India. Disputes shall first be attempted to be
            resolved through good-faith negotiation. If unresolved, disputes shall be subject to the
            exclusive jurisdiction of courts in Gurugram, Haryana, India.
          </p>
        </Section>

        <Section id="contact" label="12 — Contact">
          <p className="text-gray-400 text-sm leading-relaxed">
            For questions regarding these Terms:{" "}
            <a href="mailto:legal@hookspy.in" className="text-accent hover:underline">
              legal@hookspy.in
            </a>
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
