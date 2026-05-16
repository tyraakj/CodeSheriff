import { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'var(--bg)',
          color: 'var(--text)',
          padding: '2rem'
        }}>
          <div style={{
            maxWidth: '600px',
            textAlign: 'center'
          }}>
            <h1 style={{ fontSize: '3rem', marginBottom: '1rem' }}>⚠️</h1>
            <h2 style={{ marginBottom: '1rem' }}>Something went wrong</h2>
            <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
              {this.state.error?.message || 'An unexpected error occurred'}
            </p>
            <button
              onClick={() => window.location.reload()}
              style={{
                background: 'var(--accent)',
                color: 'var(--bg)',
                border: 'none',
                padding: '0.75rem 1.5rem',
                borderRadius: '4px',
                cursor: 'pointer',
                fontFamily: '"Courier New", monospace',
                fontWeight: 'bold'
              }}
            >
              RELOAD PAGE
            </button>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;

// Made with Bob
