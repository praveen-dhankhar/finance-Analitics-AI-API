import React, { InputHTMLAttributes } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  icon?: React.ReactNode;
}

export const Input: React.FC<InputProps> = ({ 
  label, 
  error, 
  icon,
  className = '',
  id,
  ...props 
}) => {
  const inputId = id || label?.toLowerCase().replace(/\s+/g, '-');
  
  return (
    <div className={`mb-4 w-full ${className}`} style={{ width: '100%' }}>
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-text-secondary mb-1.5" style={{ display: 'block', marginBottom: '6px', fontSize: '14px', color: 'var(--text-secondary)' }}>
          {label}
        </label>
      )}
      
      <div className="relative" style={{ position: 'relative' }}>
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" style={{ position: 'absolute', top: '50%', transform: 'translateY(-50%)', left: '12px', color: 'var(--text-secondary)' }}>
            {icon}
          </div>
        )}
        
        <input
          id={inputId}
          className={`w-full bg-[rgba(255,255,255,0.03)] border border-[rgba(255,255,255,0.1)] rounded-xl py-3 px-4 text-text-primary placeholder-[rgba(255,255,255,0.2)] focus:outline-none focus:ring-2 focus:ring-accent-primary focus:border-transparent transition-all duration-200`}
          style={{ 
            width: '100%',
            background: 'var(--glass-bg)',
            border: '1px solid var(--glass-border)',
            borderRadius: '12px',
            padding: '12px 16px',
            paddingLeft: icon ? '40px' : '16px',
            color: 'var(--text-primary)',
            boxSizing: 'border-box'
          }}
          {...props}
        />
      </div>
      
      {error && (
        <p className="mt-1.5 text-sm text-error" style={{ color: 'var(--error)', fontSize: '12px', marginTop: '4px' }}>
          {error}
        </p>
      )}
    </div>
  );
};
