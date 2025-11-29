package MONCOMPILATEUR;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Scanner;

// ===================== TOKEN ======================
class lexeme {

    String type;
    String valeur;

    lexeme(String type, String valeur) {
        this.type = type;
        this.valeur = valeur;
    }

    @Override
    public String toString() {
        return valeur + " => " + type;
    }
}

public class ANALYSELEXICALE {

    // ---------------- Automates ----------------
    public static int Numid(char car) {
        if (car == '$') {
            return 0;
        }
        if ((car >= 'a' && car <= 'z') || (car >= 'A' && car <= 'Z') || car == '_') {
            return 1;
        }
        if (car >= '0' && car <= '9') {
            return 2;
        }
        return 3;
    }

    public static int Numnbr(char car) {
        if (car >= '0' && car <= '9') {
            return 0;
        }
        if (car == '.') {
            return 1;
        }
        return 2;
    }

    public static boolean AutomateNombre(String mot) {
        int[][] matrice = {{1, 2, -1}, {1, 2, -1}, {3, -1, -1}, {3, -1, -1}};
        int ec = 0, i = 0;
        mot = mot + "#";
        while (mot.charAt(i) != '#' && matrice[ec][Numnbr(mot.charAt(i))] != -1) {
            ec = matrice[ec][Numnbr(mot.charAt(i))];
            i++;
        }
        return (mot.charAt(i) == '#' && (ec == 1 || ec == 3));
    }

    public static boolean AutomateIdentificateur(String mot) {
        int[][] matrice = {{1, -1, -1, -1}, {-1, 2, -1, -1}, {-1, 2, 3, -1}, {-1, 2, 3, -1}};
        int ec = 0, i = 0;
        mot = mot + "#";
        while (mot.charAt(i) != '#' && matrice[ec][Numid(mot.charAt(i))] != -1) {
            ec = matrice[ec][Numid(mot.charAt(i))];
            i++;
        }
        return (mot.charAt(i) == '#' && (ec == 2 || ec == 3));
    }

    public static boolean AutomateMotclé(String mot) {
        return mot.equals("if") || mot.equals("else") || mot.equals("while")
                || mot.equals("do") || mot.equals("for") || mot.equals("switch")
                || mot.equals("case") || mot.equals("foreach") || mot.equals("class") || mot.equals("function");
    }

     public static boolean estOperateur(String mot) {

        if (mot.equals("==") || mot.equals("!=") || mot.equals("++") || mot.equals("--")
                || mot.equals("&&") || mot.equals("||") || mot.equals("+=") || mot.equals("-=")
                || mot.equals("<=") || mot.equals(">=")) {
            return true;
        }

        if (mot.length() == 1) {
            char c = mot.charAt(0);
            if (c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
                    || c == '=' || c == '!' || c == '<' || c == '>' || c == '&' || c == '|') {
                return true;
            }
        }

        return false;
    }

    public static boolean AutomateMOTPERSO(String mot) {
        return mot.equals("chinza") || mot.equals("abdoudou");
    }

     public static boolean separateur(char c) {

        if (c == ' ' || c == ';' || c == '=' || c == '+' || c == '-' || c == '*' || c == '/'
                || c == '(' || c == ')' || c == '{' || c == '}' || c == '!' || c == '<' || c == '>'
                || c == '&' || c == '|' || c == ',' || c == '"' || c == '\'' || c == '\n' || c == '\r'
                || c == '#') {
            return true;
        }

        return false;
    }

 
    public static ArrayList<lexeme> analyser(String fichier) {
        ArrayList<lexeme> tokens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {
            int car;
            String mot = "";
            while ((car = br.read()) != -1) {
                char c = (char) car;

                // Commentaires
                if (c == '/') {
                    br.mark(1);
                    int next = br.read();
                    if (next == '/') {
                        while ((car = br.read()) != -1 && car != '\n');
                        continue;
                    }
                    if (next == '*') {
                        int prev = 0, curr;
                        while ((curr = br.read()) != -1) {
                            if (prev == '*' && curr == '/') {
                                break;
                            }
                            prev = curr;
                        }
                        continue;
                    }
                    br.reset();
                }

                // Chaîne
                if (c == '"') {
                    if (!mot.isEmpty()) {
                        ajouterToken(tokens, mot);
                        mot = "";
                    }
                    String chaine = "\"";
                    int nextChar;

                    while ((nextChar = br.read()) != -1) {
                        char cc = (char) nextChar;

                        
                        if (cc == '"') {
                            chaine += "\"";
                            tokens.add(new lexeme("CHAINE", chaine));
                            break;
                        }

                       
                        if (separateur(cc)) {
                            tokens.add(new lexeme("ERREUR", chaine));
                            System.out.println("Erreur");
                          
                            br.reset();
                            break;
                        }

                        chaine += cc;
                        br.mark(1); 
                    }

                  
                    if (nextChar == -1) {
                        tokens.add(new lexeme("ERREUR", chaine));
                        System.out.println("Erreur");
                    }

                    continue;
                }

               
if (c == '\'') {
    if (!mot.isEmpty()) {
        ajouterToken(tokens, mot);
        mot = "";
    }
    String carac = "'";
    int nextChar;

   
    br.mark(1);
    while ((nextChar = br.read()) != -1) {
        char cc = (char) nextChar;

      
        if (cc == '\\') {
            carac += "\\"; 
            int esc = br.read();
            if (esc == -1) { 
                tokens.add(new lexeme("ERREUR", carac));
                System.out.println("Erreur");
                break;
            }
            carac += (char) esc;
      
            br.mark(1);
            continue;
        }

      
        if (cc == '\'') {
            carac += "'";
            tokens.add(new lexeme("CARACTERE", carac));
            break;
        }

     
        if (separateur(cc)) {
            tokens.add(new lexeme("ERREUR", carac));
            System.out.println("Erreur");
          
            br.reset();
            break;
        }

        carac += cc;
      
        br.mark(1);
    }

  
    if (nextChar == -1) {
        tokens.add(new lexeme("ERREUR", carac));
        System.out.println("Erreur");
    }

    continue;
}


                // Mot
                if (!separateur(c)) {
                    mot += c;
                    continue;
                }
                if (!mot.isEmpty()) {
                    ajouterToken(tokens, mot);
                    mot = "";
                }

                // Opérateurs
                br.mark(1);
                int nextChar = br.read();
                String op = "" + c;
                if (nextChar != -1) {
                    String op2 = op + (char) nextChar;
                    if (estOperateur(op2)) {
                        tokens.add(new lexeme("OPERATEUR", op2));
                        continue;
                    } else {
                        br.reset();
                    }
                }
                if (estOperateur(op)) {
                    tokens.add(new lexeme("OPERATEUR", op));
                    continue;
                }

                // Séparateurs
                if (c != ' ' && c != '\n' && c != '\r') {
                    tokens.add(new lexeme("SEPARATEUR", "" + c));
                }
            }
            if (!mot.isEmpty()) {
                ajouterToken(tokens, mot);
            }
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
        return tokens;
    }


    private static void ajouterToken(ArrayList<lexeme> tokens, String mot) {
        if (AutomateNombre(mot)) {
            tokens.add(new lexeme("NOMBRE", mot));
        } else if (AutomateMotclé(mot)) {
            tokens.add(new lexeme("MOT_CLE", mot));
        } else if (AutomateIdentificateur(mot)) {
            tokens.add(new lexeme("IDENTIFICATEUR", mot));
        } else if (AutomateMOTPERSO(mot)) {
            tokens.add(new lexeme("MOT_CLE_PERSONNALISE", mot));
        } else {
            tokens.add(new lexeme("ERREUR", mot)); // <- détection d'erreur
            System.out.println("Erreur lexicale détectée : " + mot);
        }
    }

 
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.print("Entrez le nom du fichier à analyser (ou 'exit' pour quitter) : ");
            String fichier = sc.nextLine();
            if (fichier.equalsIgnoreCase("exit")) {
                break;
            }

            ArrayList<lexeme> lexemes = analyser(fichier);

            System.out.println("\nTABLE DES TOKENS\n");
            for (lexeme t : lexemes) {
                System.out.println(t);
            }
        }
    }
}

