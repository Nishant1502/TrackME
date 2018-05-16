package gpsapp.adminnishant.example.com.trackme;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Admin on 15-May-18.
 */

public class ListOnlineViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    public TextView txtemail;
    itemClickListener itemclicklistener;

   public void setItemclicklistener(itemClickListener itemclicklistener) {
        this.itemclicklistener = itemclicklistener;
    }


    public ListOnlineViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        txtemail=(TextView)itemView.findViewById(R.id.txt_email);
    }


    @Override
    public void onClick(View view) {

   itemclicklistener.onClick(view,getAdapterPosition());

    }
}
