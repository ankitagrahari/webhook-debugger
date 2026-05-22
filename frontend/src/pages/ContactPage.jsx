import { Mail, Instagram, ExternalLink, Zap } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function ContactPage() {
  return (
    <div className="min-h-screen bg-surface-0 flex items-center justify-center px-4">
      <div className="max-w-md w-full">

        {/* Logo */}
        <div className="flex items-center gap-2 mb-10">
          <div className="w-7 h-7 rounded-lg bg-accent flex items-center justify-center">
            <Zap size={13} className="text-white" fill="white" />
          </div>
          <Link to="/" className="font-mono text-sm font-semibold text-white">
            HookSpy
          </Link>
        </div>

        <h1 className="text-2xl font-semibold text-white mb-2">
          Get in touch
        </h1>
        <p className="text-muted text-sm mb-8">
          Questions, issues, or feedback — we respond within a few hours.
        </p>

        {/* Support email */}
        <div className="bg-surface-2 border border-border rounded-xl p-5 mb-4">
          <div className="flex items-center gap-3 mb-1">
            <Mail size={16} className="text-accent" />
            <span className="text-sm font-medium text-white">Email support</span>
          </div>
          <p className="text-muted text-sm mb-3 ml-7">
            For account issues, billing, or technical questions
          </p>

          <a href="mailto:support.hookspy@gmail.com"
            className="ml-7 font-mono text-sm text-accent hover:text-accent-hover transition-colors"
          >
            support.hookspy@gmail.com
          </a>
        </div>

        {/* Instagram */}
        <div className="bg-surface-2 border border-border rounded-xl p-5 mb-4">
          <div className="flex items-center gap-3 mb-1">
            <Instagram size={16} className="text-accent" />
            <span className="text-sm font-medium text-white">Instagram</span>
          </div>
          <p className="text-muted text-sm mb-3 ml-7">
            DMs open — fastest response
          </p>

          <a href="https://www.instagram.com/backendbrilliance"
            target="_blank"
            rel="noreferrer"
            className="ml-7 font-mono text-sm text-accent hover:text-accent-hover transition-colors flex items-center gap-1"
          >
            @backendbrilliance
            <ExternalLink size={11} />
          </a>
        </div>

        {/* Blog */}
        <div className="bg-surface-2 border border-border rounded-xl p-5 mb-8">
          <div className="flex items-center gap-3 mb-1">
            <ExternalLink size={16} className="text-accent" />
            <span className="text-sm font-medium text-white">
              Technical blog
            </span>
          </div>
          <p className="text-muted text-sm mb-3 ml-7">
            Architecture, bugs, and build in public
          </p>

          <a href="https://dynamicallyblunttech.com"
            target="_blank"
            rel="noreferrer"
            className="ml-7 text-sm text-accent hover:text-accent-hover transition-colors flex items-center gap-1">
            dynamicallyblunttech.com
            <ExternalLink size={11} />
          </a>
        </div>

        <Link
          to="/"
          className="text-sm text-muted hover:text-white transition-colors">
          ← Back to HookSpy
        </Link>
      </div>
    </div>
  )
}