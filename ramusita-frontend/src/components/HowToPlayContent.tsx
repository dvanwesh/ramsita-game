export default function HowToPlayContent() {
  return (
    <div className="space-y-2 text-sm text-slate-600">
      <p>
        Rama–Sita is a fun party game where everyone receives a random chit
        (role). <strong>Rama</strong> must figure out who{" "}
        <strong>Sita</strong> is by talking to the other players.
      </p>

      <ol className="list-decimal space-y-1 pl-5">
        <li>Create a game and share the code with your friends.</li>
        <li>Everyone joins on their phone and gets a secret chit.</li>
        <li>
          One player becomes <strong>Rama</strong>, one becomes{" "}
          <strong>Sita</strong>, and others play as Bharata,
          Shatrughna, or Hanuman.
        </li>
        <li>
          Rama talks to everyone and tries to{" "}
          <strong>guess who Sita is</strong> based on their behavior.
        </li>
        <li>
          If Rama guesses correctly, Rama earns 5000 points. If the
          guess is wrong, <strong>Sita wins the round</strong> and
          receives 5000 points.
        </li>
        <li>
          The scoreboard keeps track of points across all rounds.
        </li>
      </ol>

      <p className="text-xs text-slate-500">
        Tip: Play on a video call or in the same room so you can talk,
        bluff, and recreate the classic childhood experience.
      </p>
    </div>
  );
}
