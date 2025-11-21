import { Client } from "@stomp/stompjs";
import { WS_BASE_URL } from "../config";

export function createStompClient(
  gameId: string,
  onState: (state: any) => void
) {
  const client = new Client({
    brokerURL: WS_BASE_URL,
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
