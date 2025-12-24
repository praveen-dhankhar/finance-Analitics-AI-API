
import { OpenRouter } from '@openrouter/sdk';

const openRouter = new OpenRouter({
  apiKey: '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',
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
