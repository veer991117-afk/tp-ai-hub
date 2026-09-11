require('dotenv').config();
const express = require('express');
const cors = require('cors');
const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');

const app = express();

app.use(cors());
app.use(express.json());

const users = new Map();
const secret = process.env.JWT_SECRET || 'dev-secret';

// Health
app.get('/api/health', (_req, res) => {
  res.json({ ok: true, name: 'TP-AI-HUB' });
});

// Signup
app.post('/api/auth/signup', async (req, res) => {
  const { name, email, password } = req.body || {};

  if (!name || !email || !password) {
    return res.status(400).json({
      message: 'Name, email and password are required.'
    });
  }

  const key = email.trim().toLowerCase();

  if (users.has(key)) {
    return res.status(409).json({
      message: 'Account already exists.'
    });
  }

  const passwordHash = await bcrypt.hash(password, 10);

  const user = {
    id: Date.now().toString(),
    name: name.trim(),
    email: key,
    passwordHash
  };

  users.set(key, user);

  const token = jwt.sign(
    { sub: user.id, email: user.email },
    secret,
    { expiresIn: '7d' }
  );

  res.status(201).json({
    token,
    user: {
      id: user.id,
      name: user.name,
      email: user.email
    }
  });
});

// Login
app.post('/api/auth/login', async (req, res) => {
  const { email, password } = req.body || {};

  const user = users.get(
    (email || '').trim().toLowerCase()
  );

  if (!user || !(await bcrypt.compare(password || '', user.passwordHash))) {
    return res.status(401).json({
      message: 'Invalid email or password.'
    });
  }

  const token = jwt.sign(
    { sub: user.id, email: user.email },
    secret,
    { expiresIn: '7d' }
  );

  res.json({
    token,
    user: {
      id: user.id,
      name: user.name,
      email: user.email
    }
  });
});

// AI Chat
app.post('/api/ai/chat', async (req, res) => {
  try {
    const { message } = req.body || {};

    if (!message) {
      return res.status(400).json({
        message: 'Message is required.'
      });
    }

    const { GoogleGenAI } = await import('@google/genai');

    const ai = new GoogleGenAI({
      apiKey: process.env.GEMINI_API_KEY
    });

    const response = await ai.models.generateContent({
  model: 'gemini-3.6-flash',
  contents: `You are Astra, the official AI assistant of TP-AI-HUB.

Your name is Astra. Do not introduce yourself as Gemini or Google.
The creator of TP-AI-HUB is Rajeev Thakur.

If someone asks who created TP-AI-HUB, say:
"TP-AI-HUB was created by Rajeev Thakur."

Be friendly, helpful, accurate, and concise.
Do not invent personal information about Rajeev Thakur.

User message:
${message}`
});

    res.json({
      reply: response.text
    });

  } catch (error) {
    console.error('Gemini error:', error);

    res.status(500).json({
      message: 'AI request failed.'
    });
  }
});

// Server
const PORT = Number(process.env.PORT) || 3001;

app.listen(PORT, '0.0.0.0', () => {
  console.log(`TP-AI-HUB backend running on port ${PORT}`);
});
