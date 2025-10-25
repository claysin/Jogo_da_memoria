package com.example.jogodamemoria2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private Context context;
    private List<MemoryCard> cards;
    private OnCardClickListener listener;
    private int cardBackResource;

    public interface OnCardClickListener {
        void onCardClick(int position);
    }

    public CardAdapter(Context context, List<MemoryCard> cards, OnCardClickListener listener) {
        this.context = context;
        this.cards = cards;
        this.listener = listener;
        this.cardBackResource = R.drawable.card_back; // Imagem do verso da carta
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_layout, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        MemoryCard card = cards.get(position);

        if (card.isFlipped() || card.isMatched()) {
            holder.imageView.setImageResource(card.getImageResource());
        } else {
            holder.imageView.setImageResource(cardBackResource);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!card.isFlipped() && !card.isMatched() && listener != null) {
                listener.onCardClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cards.size();
    }

    public void updateCards() {
        notifyDataSetChanged();
    }

    public class CardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}