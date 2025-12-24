
import { OpenRouter } from '@openrouter/sdk';

const openRouter = new OpenRouter({
  apiKey: 'Your-API-Key',
  defaultHeaders: {
    'HTTP-Referer': 'http://localhost:8080',
    'X-Title': 'FinFlow AI',
  },
});

async function main() {
  try {
    const completion = await openRouter.chat.send({
      model: 'openai/gpt-4o',
      messages: [
        {
          role: 'user',
          content: 'What is the meaning of life?',
        },
      ],
      stream: false,
    });

    console.log(completion.choices[0].message.content);
  } catch (error) {
    console.error('Error:', error);
  }
}

main();
