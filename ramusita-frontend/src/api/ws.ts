import { Client } from "@stomp/stompjs";

export function createStompClient(gameId: string, onState: (state: any) => void) {
  const client = new Client({
    brokerURL: import.meta.env.VITE_WS_URL,
    reconnectDelay: 2000,
    onConnect: () => {
      client.subscribe(`/topic/games/${gameId}/state`, (msg) => {
        onState(JSON.parse(msg.body));
      });
    },
  });

  client.activate();
  return client;
}
