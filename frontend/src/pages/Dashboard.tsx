import React, { useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '../components/Card';
import { Brain, DollarSign, ArrowUpRight, ArrowDownRight } from 'lucide-react';
import api from '../api/axios';

interface Insight {
  insight: string;
}

const Dashboard: React.FC = () => {
  const [insights, setInsights] = useState<Insight | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchInsights = async () => {
      try {
        const response = await api.get('/forecasts/insights');
        setInsights(response.data);
      } catch (error) {
        console.error('Failed to fetch insights', error);
      } finally {
        setLoading(false);
      }
    };
    
    // In a real scenario we might have an API for summary stats. 
    // Here we use mock for the stats widgets and real API for insights.
    fetchInsights();
  }, []);

  return (
    <div className="animate-fade-in">
      <header style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '28px', fontWeight: 'bold' }}>Dashboard Overview</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: '8px' }}>Your AI-powered financial summary</p>
      </header>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '24px', marginBottom: '32px' }}>
        {/* Mock Stats Cards */}
        <Card>
          <CardContent className="flex flex-col justify-between h-full">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
              <div style={{ padding: '12px', background: 'rgba(20, 184, 166, 0.1)', borderRadius: '12px', color: 'var(--accent-tertiary)' }}>
                <DollarSign size={24} />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', color: 'var(--success)', fontSize: '14px', fontWeight: 500 }}>
                <ArrowUpRight size={16} style={{ marginRight: '4px' }} />
                +12.5%
              </div>
            </div>
            <div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginBottom: '4px' }}>Total Balance</p>
              <h2 style={{ fontSize: '28px', fontWeight: 'bold' }}>$24,562.00</h2>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="flex flex-col justify-between h-full">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '16px' }}>
              <div style={{ padding: '12px', background: 'rgba(236, 72, 153, 0.1)', borderRadius: '12px', color: 'var(--accent-secondary)' }}>
                <ArrowDownRight size={24} />
              </div>
              <div style={{ display: 'flex', alignItems: 'center', color: 'var(--error)', fontSize: '14px', fontWeight: 500 }}>
                <ArrowUpRight size={16} style={{ marginRight: '4px' }} />
                +4.2%
              </div>
            </div>
            <div>
              <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginBottom: '4px' }}>Monthly Expenses</p>
              <h2 style={{ fontSize: '28px', fontWeight: 'bold' }}>$4,210.50</h2>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* AI Insights Section */}
      <Card glow>
        <CardHeader style={{ display: 'flex', flexDirection: 'row', alignItems: 'center' }}>
          <div style={{ padding: '8px', background: 'linear-gradient(135deg, var(--accent-primary), var(--accent-secondary))', borderRadius: '8px', marginRight: '16px' }}>
            <Brain size={20} color="white" />
          </div>
          <CardTitle>Gemini AI Insights</CardTitle>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div style={{ display: 'flex', justifyContent: 'center', padding: '40px' }}>
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-accent-primary"></div>
            </div>
          ) : (
            <div style={{ lineHeight: '1.6', color: 'var(--text-primary)', fontSize: '15px' }}>
              {insights?.insight ? (
                <div dangerouslySetInnerHTML={{ __html: insights.insight.replace(/\\n/g, '<br/>') }} />
              ) : (
                <p>No insights available at the moment. Add more financial data to generate analysis.</p>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default Dashboard;
