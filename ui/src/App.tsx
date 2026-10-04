import { useEffect } from 'react';
import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import WelcomePage from './pages/WelcomePage';
import QuizPage from './pages/QuizPage';
import ResultPage from './pages/ResultPage';
import HistoryPage from './pages/HistoryPage';
import AssessmentPage from './pages/AssessmentPage';

const KANBAN_URL = import.meta.env.VITE_KANBAN_URL ?? 'http://localhost:5174';

function KanbanRedirect() {
  useEffect(() => {
    window.location.replace(KANBAN_URL);
  }, []);

  return null;
}

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<WelcomePage />} />
        <Route path="/quiz" element={<QuizPage />} />
        <Route path="/result" element={<ResultPage />} />
        <Route path="/history" element={<HistoryPage />} />
        <Route path="/assessment" element={<AssessmentPage />} />
        <Route path="/board" element={<KanbanRedirect />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
