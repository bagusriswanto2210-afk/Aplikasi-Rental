package com.example.data.model

data class BundleItemSpec(
    val itemNameQuery: String,
    val defaultItemName: String,
    val category: String,
    val qty: Int,
    val singlePricePerDay: Long
)

data class BundlePreset(
    val id: String,
    val name: String,
    val tagline: String,
    val badge: String,
    val bundledPricePerDay: Long,
    val normalPricePerDay: Long,
    val items: List<BundleItemSpec>
) {
    val savingsPerDay: Long
        get() = (normalPricePerDay - bundledPricePerDay).coerceAtLeast(0L)

    companion object {
        val PRESET_BUNDLES = listOf(
            BundlePreset(
                id = "bundle_summit_2p",
                name = "Paket Muncak Berdua",
                tagline = "Cocok untuk pendakian duet / romantis di gunung",
                badge = "🌲 Terpopuler (Hemat 20%)",
                bundledPricePerDay = 85000,
                normalPricePerDay = 110000,
                items = listOf(
                    BundleItemSpec("Tenda Ultralight Kap 2", "Tenda Ultralight Kap 2 Naturehike", "Tenda", 1, 35000),
                    BundleItemSpec("Sleeping Bag Polar", "Sleeping Bag Polar Bulu Tebal", "Sleeping Gear", 2, 15000),
                    BundleItemSpec("Matras Foil", "Matras Foil Aluminium Double", "Sleeping Gear", 2, 7000),
                    BundleItemSpec("Kompor Portable", "Kompor Portable Mawar Windproof", "Cooking", 1, 15000),
                    BundleItemSpec("Nesting Cooking Set", "Nesting Cooking Set DS-308 (4 in 1)", "Cooking", 1, 20000)
                )
            ),
            BundlePreset(
                id = "bundle_family_4p",
                name = "Paket Family Berempat",
                tagline = "Lengkap dan nyaman untuk kemah keluarga di bukit / camping ground",
                badge = "🏕️ Rekomendasi (Hemat 25%)",
                bundledPricePerDay = 135000,
                normalPricePerDay = 180000,
                items = listOf(
                    BundleItemSpec("Tenda Dome Kap 4", "Tenda Dome Kap 4 Arpenaz", "Tenda", 1, 45000),
                    BundleItemSpec("Sleeping Bag Polar", "Sleeping Bag Polar Bulu Tebal", "Sleeping Gear", 4, 15000),
                    BundleItemSpec("Matras Foil", "Matras Foil Aluminium Double", "Sleeping Gear", 2, 7000),
                    BundleItemSpec("Kompor Portable", "Kompor Portable Mawar Windproof", "Cooking", 1, 15000),
                    BundleItemSpec("Nesting Cooking Set", "Nesting Cooking Set DS-308 (4 in 1)", "Cooking", 1, 20000),
                    BundleItemSpec("Lampu Tenda", "Lampu Tenda Rechargeable Solar", "Penerangan", 1, 10000)
                )
            ),
            BundlePreset(
                id = "bundle_solo_expedition",
                name = "Paket Solo Trekker",
                tagline = "Ultralight & ringkas untuk summit attack sendirian",
                badge = "⛰️ Ringan & Cepat",
                bundledPricePerDay = 65000,
                normalPricePerDay = 85000,
                items = listOf(
                    BundleItemSpec("Carrier Osprey", "Carrier Osprey Atmos 50L AG", "Carrier", 1, 40000),
                    BundleItemSpec("Sleeping Bag Polar", "Sleeping Bag Polar Bulu Tebal", "Sleeping Gear", 1, 15000),
                    BundleItemSpec("Headlamp Petzl", "Headlamp Petzl Tikkina 300L", "Penerangan", 1, 15000),
                    BundleItemSpec("Trekking Pole", "Trekking Pole Duralumin Anti-Shock", "Aksesoris", 1, 10000),
                    BundleItemSpec("Matras Foil", "Matras Foil Aluminium Double", "Sleeping Gear", 1, 7000)
                )
            ),
            BundlePreset(
                id = "bundle_chill_bbq",
                name = "Paket Santai & Masak Rimba",
                tagline = "Fokus santai, kopi senja, dan masak di api unggun",
                badge = "🔥 Vibe Api Unggun",
                bundledPricePerDay = 60000,
                normalPricePerDay = 75000,
                items = listOf(
                    BundleItemSpec("Kompor Portable", "Kompor Portable Mawar Windproof", "Cooking", 1, 15000),
                    BundleItemSpec("Nesting Cooking Set", "Nesting Cooking Set DS-308 (4 in 1)", "Cooking", 1, 20000),
                    BundleItemSpec("Kursi Lipat", "Kursi Lipat Naturehike Ultralight", "Aksesoris", 2, 15000),
                    BundleItemSpec("Lampu Tenda", "Lampu Tenda Rechargeable Solar", "Penerangan", 1, 10000)
                )
            )
        )
    }
}
