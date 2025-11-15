import java.io.*;
import java.util.*;

class Player {
    String name;
    double defaultPoints, avg, formAvg, venueCountryAvg, oppositionAvg, matchupAvg, weightedScore;

    public Player(String name, double defaultPoints, double avg, double formAvg,
                  double venueCountryAvg, double oppositionAvg, double matchupAvg) {
        this.name = name;
        this.defaultPoints = defaultPoints;
        this.avg = avg;
        this.formAvg = formAvg;
        this.venueCountryAvg = venueCountryAvg;
        this.oppositionAvg = oppositionAvg;
        this.matchupAvg = matchupAvg;

        calculateWeightedScore();
    }

    private void calculateWeightedScore() {
        this.weightedScore =
                (defaultPoints * 0.25) +
                (avg * 0.20) +
                (formAvg * 0.20) +
                (venueCountryAvg * 0.20) +
                (oppositionAvg * 0.10) +
                (matchupAvg * 0.10);
    }

    @Override
    public String toString() {
        return name + " (Weighted: " + String.format("%.2f", weightedScore)
                + ", Default: " + defaultPoints + ")";
    }
}

public class FantasyProject {
    public static void main(String[] args) {
        List<Player> players = new ArrayList<>();
        String fileName = "AUS_Vs_IND_T20I_14.txt";

        // 🧾 Reading file input
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Handle both comma and space separated values
                String[] data = line.split("[,\\s]+");
                if (data.length != 7) {
                    System.out.println("⚠️ Skipping invalid line: " + line);
                    continue;
                }

                String name = data[0];
                double defaultPoints = Double.parseDouble(data[1]);
                double avg = Double.parseDouble(data[2]);
                double formAvg = Double.parseDouble(data[3]);
                double venueCountryAvg = Double.parseDouble(data[4]);
                double oppositionAvg = Double.parseDouble(data[5]);
                double matchupAvg = Double.parseDouble(data[6]);

                players.add(new Player(name, defaultPoints, avg, formAvg,
                        venueCountryAvg, oppositionAvg, matchupAvg));
            }
        } catch (IOException e) {
            System.out.println("❌ Error reading file: " + e.getMessage());
            return;
        }

        if (players.isEmpty()) {
            System.out.println("❌ No players found! Please check your file format.");
            return;
        }

        // Sorting
        List<Player> sortedByWeighted = new ArrayList<>(players);
        sortedByWeighted.sort((p1, p2) -> Double.compare(p2.weightedScore, p1.weightedScore));

        List<Player> sortedByDefault = new ArrayList<>(players);
        sortedByDefault.sort((p1, p2) -> Double.compare(p2.defaultPoints, p1.defaultPoints));

        // Team setup
        List<Player> userTeam = new ArrayList<>();
        List<Player> computerTeam = new ArrayList<>();
        Set<String> selectedPlayers = new HashSet<>();
        try (Scanner scanner = new Scanner(System.in)) {
            // 🔹 Ask who starts first
            System.out.print("Do you want to start first? (yes/no): ");
            boolean userStartsFirst = scanner.next().equalsIgnoreCase("yes");

            int userDefaultPoints = userStartsFirst ? -16 : 14;
            int computerDefaultPoints = userStartsFirst ? 16 : -14;

            System.out.println("\nAvailable Players (Sorted by Weighted Score):");
            for (int i = 0; i < sortedByWeighted.size(); i++) {
                System.out.println((i + 1) + ". " + sortedByWeighted.get(i));
            }

            System.out.println("\n🎯 Player Selection Begins:");
            for (int i = 0; i < 4; i++) {
                // Computer's turn (if user doesn't start first)
                if (!userStartsFirst) {
                    for (Player p : sortedByDefault) {
                        if (!selectedPlayers.contains(p.name)) {
                            computerTeam.add(p);
                            selectedPlayers.add(p.name);
                            System.out.println("🤖 Computer selected: " + p.name);
                            break;
                        }
                    }
                }

                // User's turn
                int choice;
                while (true) {
                    System.out.print("Select player " + (i + 1) + ": ");
                    choice = scanner.nextInt();
                    if (choice >= 1 && choice <= sortedByWeighted.size()) {
                        Player selected = sortedByWeighted.get(choice - 1);
                        if (!selectedPlayers.contains(selected.name)) {
                            userTeam.add(selected);
                            selectedPlayers.add(selected.name);
                            break;
                        } else {
                            System.out.println("Player already selected. Choose another.");
                        }
                    } else {
                        System.out.println("Invalid choice. Try again.");
                    }
                }

                // Computer's turn (if user starts first)
                if (userStartsFirst) {
                    for (Player p : sortedByDefault) {
                        if (!selectedPlayers.contains(p.name)) {
                            computerTeam.add(p);
                            selectedPlayers.add(p.name);
                            System.out.println("🤖 Computer selected: " + p.name);
                            break;
                        }
                    }
                }
            }

            // Results
            System.out.println("\n🏏 Final Teams:");
            System.out.println("Your Team: " + userTeam);
            System.out.println("Computer's Team: " + computerTeam);

            double userScore = userDefaultPoints + userTeam.stream().mapToDouble(p -> p.weightedScore).sum();
            double computerScore = computerDefaultPoints + computerTeam.stream().mapToDouble(p -> p.weightedScore).sum();

            System.out.println("\n📊 Final Scores:");
            System.out.println("Your Total Score: " + String.format("%.2f", userScore));
            System.out.println("Computer's Total Score: " + String.format("%.2f", computerScore));

            if (userScore > computerScore) System.out.println("🎉 You WIN!");
            else if (userScore < computerScore) System.out.println("🤖 Computer WINS!");
            else System.out.println("🤝 It's a DRAW!");
        }

        // Underrated and overrated players
        List<Player> underratedPlayers = new ArrayList<>();
        List<Player> overratedPlayers = new ArrayList<>();

        for (Player p : players) {
            if (p.weightedScore > p.defaultPoints + 10) {
                underratedPlayers.add(p);
            } else if (p.defaultPoints > p.weightedScore - 5) {
                overratedPlayers.add(p);
            }
        }

        System.out.println("\n📈 Underrated Players (Weighted Score > Default + 5):");
        if (underratedPlayers.isEmpty()) System.out.println("None.");
        else underratedPlayers.forEach(System.out::println);

        System.out.println("\n📉 Overrated Players (Default > Weighted Score):");
        if (overratedPlayers.isEmpty()) System.out.println("None.");
        else overratedPlayers.forEach(System.out::println);
    }
}
