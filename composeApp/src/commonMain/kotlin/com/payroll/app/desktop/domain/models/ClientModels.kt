package com.payroll.app.desktop.domain.models

data class EmployeeSimple(
    val id: String,
    val name: String,
    val email: String,
    val clientCount: Int = 0
)

data class ClientSimple(
    val id: String,
    val name: String,
    val price: Double,
    val employeePrice: Double,
    val companyPrice: Double,
    val employeeId: String
)

// Dummy Data
object DummyData {
    val employees = listOf(
        EmployeeSimple("1", "Αναστασία Καλαμποκά", "ana@example.com", 12),
        EmployeeSimple("2", "Ανθή Τουφεξή", "anthi@example.com", 8),
        EmployeeSimple("3", "Ισμήνη Κατσαρού", "ismini@example.com", 15),
        EmployeeSimple("4", "Ζέτα Σισκοπούλου", "zeta@example.com", 10),
        EmployeeSimple("5", "Ιωάννα Προκοπίου", "ioanna@example.com", 7),
        EmployeeSimple("6", "Μαριάννα Τριανταφύλλου", "marianna@example.com", 9),
        EmployeeSimple("7", "Αγγελική Γκουντοπούλου", "aggeliki@example.com", 14)
    )

    val clients = listOf(
        // Αναστασία's clients
        ClientSimple("c1", "Κωνσταντίνος Παπαδόπουλος", 45.0, 30.0, 15.0, "1"),
        ClientSimple("c2", "Μαρία Γεωργίου", 50.0, 35.0, 15.0, "1"),
        ClientSimple("c3", "Νίκος Αλεξόπουλος", 45.0, 30.0, 15.0, "1"),
        ClientSimple("c4", "Ελένη Δημητρίου", 50.0, 35.0, 15.0, "1"),

        // Ανθή's clients
        ClientSimple("c5", "Γιώργος Ιωάννου", 40.0, 28.0, 12.0, "2"),
        ClientSimple("c6", "Σοφία Κωνσταντίνου", 45.0, 30.0, 15.0, "2"),
        ClientSimple("c7", "Δημήτρης Μιχαηλίδης", 40.0, 28.0, 12.0, "2"),

        // Ισμήνη's clients
        ClientSimple("c8", "Αλέξανδρος Νικολάου", 50.0, 35.0, 15.0, "3"),
        ClientSimple("c9", "Κατερίνα Παναγιώτου", 45.0, 30.0, 15.0, "3"),
        ClientSimple("c10", "Πέτρος Σταυρίδης", 50.0, 35.0, 15.0, "3"),

        // Ζέτα's clients
        ClientSimple("c11", "Άννα Χριστοδούλου", 40.0, 28.0, 12.0, "4"),
        ClientSimple("c12", "Βασίλης Θεοδώρου", 45.0, 30.0, 15.0, "4"),

        // Ιωάννα's clients
        ClientSimple("c13", "Χριστίνα Αντωνίου", 40.0, 28.0, 12.0, "5"),
        ClientSimple("c14", "Παναγιώτης Γρηγορίου", 45.0, 30.0, 15.0, "5"),

        // Μαριάννα's clients
        ClientSimple("c15", "Δέσποινα Βλάχου", 50.0, 35.0, 15.0, "6"),
        ClientSimple("c16", "Στέφανος Κυριακίδης", 45.0, 30.0, 15.0, "6"),

        // Αγγελική's clients
        ClientSimple("c17", "Ευαγγελία Παυλίδου", 45.0, 30.0, 15.0, "7"),
        ClientSimple("c18", "Θανάσης Πετρίδης", 50.0, 35.0, 15.0, "7")
    )
}