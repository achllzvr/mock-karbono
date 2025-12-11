package com.achllzvr.mockkarbono.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.achllzvr.mockkarbono.R;
import com.achllzvr.mockkarbono.ui.adapters.MarketplaceAdapter;
import com.achllzvr.mockkarbono.ui.adapters.MarketItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Marketplace Fragment - Soft Pop / Duolingo Style
 * Displays tree planting, appliances, and thrift items for carbon offset
 */
public class MarketplaceFragment extends Fragment {

    private RecyclerView rvMarketplaceItems;
    private MarketplaceAdapter adapter;

    private TextView tabPlantTrees;
    private TextView tabAppliances;
    private TextView tabThrift;

    private int currentTab = 0; // 0 = Plant Trees, 1 = Appliances, 2 = Thrift

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        // Bind views
        rvMarketplaceItems = view.findViewById(R.id.rvMarketplaceItems);
        tabPlantTrees = view.findViewById(R.id.tabPlantTrees);
        tabAppliances = view.findViewById(R.id.tabAppliances);
        tabThrift = view.findViewById(R.id.tabThrift);

        // Setup RecyclerView
        rvMarketplaceItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MarketplaceAdapter(getTreeItems(), item -> {
            Toast.makeText(requireContext(), "Added to cart: " + item.getTitle(), Toast.LENGTH_SHORT).show();
        });
        rvMarketplaceItems.setAdapter(adapter);

        // Setup tab click listeners
        setupTabListeners();

        return view;
    }

    private void setupTabListeners() {
        tabPlantTrees.setOnClickListener(v -> {
            setActiveTab(0);
            adapter.updateItems(getTreeItems());
        });

        tabAppliances.setOnClickListener(v -> {
            setActiveTab(1);
            adapter.updateItems(getApplianceItems());
        });

        tabThrift.setOnClickListener(v -> {
            setActiveTab(2);
            adapter.updateItems(getThriftItems());
        });
    }

    private void setActiveTab(int tabIndex) {
        currentTab = tabIndex;

        // Reset all tabs to inactive state
        tabPlantTrees.setBackgroundResource(android.R.color.transparent);
        tabPlantTrees.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tabAppliances.setBackgroundResource(android.R.color.transparent);
        tabAppliances.setTextColor(getResources().getColor(R.color.text_secondary, null));
        tabThrift.setBackgroundResource(android.R.color.transparent);
        tabThrift.setTextColor(getResources().getColor(R.color.text_secondary, null));

        // Set active tab
        TextView activeTab;
        switch (tabIndex) {
            case 1:
                activeTab = tabAppliances;
                break;
            case 2:
                activeTab = tabThrift;
                break;
            default:
                activeTab = tabPlantTrees;
                break;
        }
        activeTab.setBackgroundResource(R.drawable.bg_button_green);
        activeTab.setTextColor(getResources().getColor(android.R.color.white, null));
    }

    // Mock data: Tree items
    private List<MarketItem> getTreeItems() {
        List<MarketItem> items = new ArrayList<>();
        items.add(new MarketItem("Mango Tree", "Offsets 12kg CO₂/year", "₱500 + 🌳2", R.drawable.ic_tree));
        items.add(new MarketItem("Narra Tree", "Offsets 18kg CO₂/year • Native", "₱750 + 🌳3", R.drawable.ic_tree));
        items.add(new MarketItem("Coconut Palm", "Offsets 8kg CO₂/year", "₱350 + 🌳1", R.drawable.ic_tree));
        items.add(new MarketItem("Bamboo Grove", "Offsets 25kg CO₂/year • Fast growing", "₱600 + 🌳4", R.drawable.ic_tree));
        items.add(new MarketItem("Acacia Tree", "Offsets 15kg CO₂/year", "₱450 + 🌳2", R.drawable.ic_tree));
        return items;
    }

    // Mock data: Appliance items
    private List<MarketItem> getApplianceItems() {
        List<MarketItem> items = new ArrayList<>();
        items.add(new MarketItem("Solar Panel Kit", "Saves 50kg CO₂/month", "₱15,000 + 🌳10", R.drawable.ic_appliance));
        items.add(new MarketItem("LED Bulb Pack (10)", "Saves 5kg CO₂/month", "₱500 + 🌳1", R.drawable.ic_appliance));
        items.add(new MarketItem("Smart Thermostat", "Saves 8kg CO₂/month", "₱3,500 + 🌳3", R.drawable.ic_appliance));
        items.add(new MarketItem("Energy Monitor", "Track your usage", "₱1,200 + 🌳1", R.drawable.ic_appliance));
        items.add(new MarketItem("Inverter AC Unit", "40% more efficient", "₱25,000 + 🌳15", R.drawable.ic_appliance));
        return items;
    }

    // Mock data: Thrift items
    private List<MarketItem> getThriftItems() {
        List<MarketItem> items = new ArrayList<>();
        items.add(new MarketItem("Vintage Denim Jacket", "Size M • Like new", "₱450 + 🌳1", R.drawable.ic_shop));
        items.add(new MarketItem("Preloved Sneakers", "Size 42 • Good condition", "₱800 + 🌳1", R.drawable.ic_shop));
        items.add(new MarketItem("Eco-friendly Tote Bag", "Handmade • Organic cotton", "₱250 + 🌳1", R.drawable.ic_shop));
        items.add(new MarketItem("Refurbished Tablet", "Works perfectly", "₱5,000 + 🌳3", R.drawable.ic_shop));
        items.add(new MarketItem("Upcycled Furniture", "Unique piece", "₱2,500 + 🌳2", R.drawable.ic_shop));
        return items;
    }
}

