import { Link } from "react-router-dom";

const Section = ({ id, label, children }) => (
  <section id={id} className="mb-10">
    <h2 className="font-mono text-xs font-semibold text-teal-400 tracking-widest uppercase mb-3 pb-2 border-b border-gray-800">
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
              className="font-mono text-xs text-teal-400 uppercase tracking-widest text-left px-3 py-2 border border-gray-800 bg-gray-900"
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

const Tag = ({ type }) => {
  const styles = {
    yes: "text-green-400 font-semibold",
    no: "text-red-400 font-semibold",
    partial: "text-amber-400 font-semibold",
  };
  const labels = { yes: "✓ Full Refund", no: "✗ No Refund", partial: "~ Pro-rated / Credit" };
  return <span className={styles[type]}>{labels[type]}</span>;
};

export default function RefundPolicyPage() {
  return (
    <div className="min-h-screen bg-gray-950 text-gray-100">
      {/* Header */}
      <header className="sticky top-0 z-50 border-b border-gray-800 bg-gray-950/90 backdrop-blur-md px-6 py-4 flex items-center gap-3">
        <Link to="/" className="font-mono font-bold text-teal-400 text-lg">
          Hook<span className="text-gray-100">Spy</span>
        </Link>
        <span className="font-mono text-xs font-semibold bg-teal-400/10 text-teal-400 border border-teal-400/30 px-2 py-0.5 rounded tracking-widest uppercase">
          Legal
        </span>
      </header>

      <main className="max-w-3xl mx-auto px-6 py-12 pb-20">
        <h1 className="font-mono text-3xl font-bold text-gray-100 mb-1">
          Refund &amp; <span className="text-teal-400">Cancellation Policy</span>
        </h1>
        <div className="flex flex-wrap gap-4 font-mono text-xs text-gray-500 mb-10">
          <span>Effective: 23 May 2025</span>
          <span>Last Updated: 23 May 2025</span>
          <span>hookspy.in</span>
        </div>

        <div className="bg-emerald-950/30 border border-emerald-500/30 border-l-2 border-l-emerald-400 rounded-r-md p-4 mb-10 text-sm text-emerald-200">
          HookSpy offers a fair and transparent refund policy. We provide a 7-day money-back window
          for new subscribers and pro-rated cancellations on annual plans.
        </div>

        <Section id="plans" label="01 — Subscription Plans">
          <Table
            headers={["Plan", "Billing Cycle", "Price (INR)", "Includes GST"]}
            rows={[
              ["Free", "—", "₹0", "—"],
              ["Pro", "Monthly", "₹299/month", "Yes"],
              ["Pro", "Annual", "₹2,999/year", "Yes"],
              ["Team", "Monthly", "₹799/month", "Yes"],
              ["Team", "Annual", "₹7,999/year", "Yes"],
            ]}
          />
          <p className="text-gray-400 text-sm">
            All prices are inclusive of applicable GST (18%). A valid GST invoice will be issued for
            every transaction.
          </p>
        </Section>

        <Section id="moneyback" label="02 — 7-Day Money-Back Guarantee">
          <p className="text-gray-400 text-sm mb-3">
            New subscribers to any paid plan are eligible for a full refund if requested within{" "}
            <strong className="text-gray-200">7 calendar days</strong> of the initial payment date,
            provided:
          </p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm mb-3">
            <li>The request is the first refund from your account</li>
            <li>The account has not been suspended for policy violations</li>
            <li>
              The request is made by email to{" "}
              <a href="mailto:support.hookspy@gmail.com" className="text-teal-400 hover:underline">
                support.hookspy@gmail.com
              </a>{" "}
              within the 7-day window
            </li>
          </ul>
          <p className="text-gray-400 text-sm">
            Refunds are processed to the original payment method within 5–7 business days.
          </p>
        </Section>

        <Section id="monthly" label="03 — Monthly Subscription Cancellations">
          <p className="text-gray-400 text-sm mb-3">
            Cancel anytime from{" "}
            <strong className="text-gray-300">Settings → Billing → Cancel Plan</strong> in your
            account dashboard.
          </p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm">
            <li>Cancellation takes effect at the end of the current billing period</li>
            <li>You retain full access to paid features until the period ends</li>
            <li>No pro-rated refund is issued for unused days on monthly plans beyond the 7-day window</li>
            <li>Your account downgrades to Free tier automatically after cancellation</li>
          </ul>
        </Section>

        <Section id="annual" label="04 — Annual Subscription Cancellations">
          <p className="text-gray-400 text-sm mb-3">
            For annual plans cancelled after the 7-day money-back window:
          </p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm mb-3">
            <li>
              A pro-rated refund is issued for{" "}
              <strong className="text-gray-200">complete unused months</strong> remaining in the
              annual term
            </li>
            <li>
              Formula:{" "}
              <code className="font-mono text-xs bg-gray-900 text-teal-300 px-1.5 py-0.5 rounded">
                (Months remaining × Monthly equivalent) − ₹49 processing fee
              </code>
            </li>
            <li>
              Example: Cancel after 5 months on annual Pro (₹2,999): 7 × ₹250 − ₹49 ={" "}
              <strong className="text-green-400">₹1,701 refund</strong>
            </li>
          </ul>
        </Section>

        <Section id="matrix" label="05 — Refund Eligibility Matrix">
          <Table
            headers={["Scenario", "Outcome"]}
            rows={[
              ["New subscription, within 7 days", <Tag type="yes" />],
              ["Annual plan, cancelled after 7 days", <Tag type="partial" />],
              ["Monthly plan, cancelled after 7 days", <Tag type="no" />],
              ["Account suspended for policy violation", <Tag type="no" />],
              ["Downgrade to Free plan", <Tag type="no" />],
              ["Service outage exceeding 24 hours (SLA breach)", <Tag type="partial" />],
              ["Duplicate charge / billing error", <Tag type="yes" />],
            ]}
          />
        </Section>

        <Section id="sla" label="06 — SLA Credits">
          <p className="text-gray-400 text-sm leading-relaxed">
            If HookSpy experiences unplanned downtime exceeding 24 cumulative hours within a
            calendar month, affected paid subscribers will receive a service credit proportional to
            the downtime. Credits are applied to the next billing cycle and are not redeemable as
            cash.
          </p>
        </Section>

        <Section id="howto" label="07 — How to Request a Refund">
          <p className="text-gray-400 text-sm mb-3">
            Email{" "}
            <a href="mailto:support.hookspy@gmail.com" className="text-teal-400 hover:underline">
              support.hookspy@gmail.com
            </a>{" "}
            with:
          </p>
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm mb-3">
            <li>
              Subject:{" "}
              <code className="font-mono text-xs bg-gray-900 text-teal-300 px-1.5 py-0.5 rounded">
                Refund Request — [Your Account Email]
              </code>
            </li>
            <li>Your registered account email address</li>
            <li>Razorpay transaction ID or receipt number</li>
            <li>Reason for the refund request</li>
          </ul>
          <p className="text-gray-400 text-sm">
            We respond within 2 business days and process eligible refunds within 5–7 business days.
          </p>
        </Section>

        <Section id="nonrefundable" label="08 — Non-Refundable Situations">
          <ul className="list-disc list-inside space-y-1.5 text-gray-400 text-sm">
            <li>
              Accounts terminated for violations of the{" "}
              <Link to="/terms" className="text-teal-400 hover:underline">
                Terms &amp; Conditions
              </Link>{" "}
              (including malicious use or proxying illegal traffic)
            </li>
            <li>Requests made after the applicable refund window has lapsed</li>
            <li>Free plan downgrades</li>
            <li>Promotional or discounted subscription periods</li>
          </ul>
        </Section>

        <Section id="disputes" label="09 — Disputes &amp; Chargebacks">
          <p className="text-gray-400 text-sm leading-relaxed">
            We encourage you to contact us directly before initiating a chargeback with your bank.
            Chargebacks may result in account suspension pending investigation. We are committed to
            resolving legitimate billing concerns promptly.
          </p>
        </Section>

        <Section id="contact" label="10 — Contact">
          <p className="text-gray-400 text-sm leading-relaxed">
            Billing queries &amp; refund requests:{" "}
            <a href="mailto:support.hookspy@gmail.com" className="text-teal-400 hover:underline">
              support.hookspy@gmail.com
            </a>
            <br />
            Response time: 2 business days · Support hours: Mon–Fri, 10:00–18:00 IST
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
        <Link to="/terms" className="hover:text-teal-400 transition-colors">Terms &amp; Conditions</Link>
        <Link to="/privacy-policy" className="hover:text-teal-400 transition-colors">Privacy &amp; Cookies</Link>
        <Link to="/refund-policy" className="hover:text-teal-400 transition-colors">Refund &amp; Cancellation</Link>
        <a href="mailto:support.hookspy@gmail.com" className="hover:text-teal-400 transition-colors">Support</a>
      </nav>
      <p className="text-gray-600">© 2025 HookSpy. All rights reserved.</p>
    </footer>
  );
}
