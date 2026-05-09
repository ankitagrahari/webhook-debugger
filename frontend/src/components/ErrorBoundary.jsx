import { Component } from 'react'

export default class ErrorBoundary extends Component {
  constructor(props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error }
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-surface-0 flex items-center justify-center px-4">
          <div className="text-center max-w-sm">
            <div className="text-4xl mb-4">⚠️</div>
            <h2 className="text-lg font-semibold text-white mb-2">Something went wrong</h2>
            <p className="text-sm text-muted mb-6 font-mono">
              {this.state.error?.message || 'Unexpected error'}
            </p>
            <button
              onClick={() => window.location.href = '/'}
              className="bg-accent hover:bg-accent-hover text-white text-sm px-4 py-2 rounded-lg transition-colors"
            >
              Go home
            </button>
          </div>
        </div>
      )
    }
    return this.props.children
  }
}