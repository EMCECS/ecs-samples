namespace ecspics_loader
{
    partial class Main
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.cmdUpload = new System.Windows.Forms.Button();
            this.lblBucket = new System.Windows.Forms.Label();
            this.txtBucketName = new System.Windows.Forms.TextBox();
            this.txtStatus = new System.Windows.Forms.TextBox();
            this.lblUserName = new System.Windows.Forms.Label();
            this.txtUserName = new System.Windows.Forms.TextBox();
            this.lblSecretKey = new System.Windows.Forms.Label();
            this.txtSecretKey = new System.Windows.Forms.TextBox();
            this.cmbPictureCount = new System.Windows.Forms.ComboBox();
            this.lblPictures = new System.Windows.Forms.Label();
            this.pictureBox1 = new System.Windows.Forms.PictureBox();
            this.lblIpPort = new System.Windows.Forms.Label();
            this.txtIpAddress = new System.Windows.Forms.TextBox();
            this.cmbPort = new System.Windows.Forms.ComboBox();
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).BeginInit();
            this.SuspendLayout();
            // 
            // cmdUpload
            // 
            this.cmdUpload.Location = new System.Drawing.Point(13, 415);
            this.cmdUpload.Name = "cmdUpload";
            this.cmdUpload.Size = new System.Drawing.Size(277, 39);
            this.cmdUpload.TabIndex = 7;
            this.cmdUpload.Text = "Upload";
            this.cmdUpload.UseVisualStyleBackColor = true;
            this.cmdUpload.Click += new System.EventHandler(this.cmdUpload_Click);
            // 
            // lblBucket
            // 
            this.lblBucket.AutoSize = true;
            this.lblBucket.Location = new System.Drawing.Point(10, 365);
            this.lblBucket.Name = "lblBucket";
            this.lblBucket.Size = new System.Drawing.Size(72, 13);
            this.lblBucket.TabIndex = 1;
            this.lblBucket.Text = "Bucket Name";
            // 
            // txtBucketName
            // 
            this.txtBucketName.Location = new System.Drawing.Point(89, 358);
            this.txtBucketName.Name = "txtBucketName";
            this.txtBucketName.Size = new System.Drawing.Size(201, 20);
            this.txtBucketName.TabIndex = 5;
            // 
            // txtStatus
            // 
            this.txtStatus.Font = new System.Drawing.Font("Courier New", 8F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(0)));
            this.txtStatus.Location = new System.Drawing.Point(313, 16);
            this.txtStatus.Multiline = true;
            this.txtStatus.Name = "txtStatus";
            this.txtStatus.ScrollBars = System.Windows.Forms.ScrollBars.Vertical;
            this.txtStatus.Size = new System.Drawing.Size(459, 438);
            this.txtStatus.TabIndex = 8;
            // 
            // lblUserName
            // 
            this.lblUserName.AutoSize = true;
            this.lblUserName.Location = new System.Drawing.Point(10, 314);
            this.lblUserName.Name = "lblUserName";
            this.lblUserName.Size = new System.Drawing.Size(55, 13);
            this.lblUserName.TabIndex = 4;
            this.lblUserName.Text = "Username";
            // 
            // txtUserName
            // 
            this.txtUserName.Location = new System.Drawing.Point(89, 307);
            this.txtUserName.Name = "txtUserName";
            this.txtUserName.Size = new System.Drawing.Size(201, 20);
            this.txtUserName.TabIndex = 3;
            // 
            // lblSecretKey
            // 
            this.lblSecretKey.AutoSize = true;
            this.lblSecretKey.Location = new System.Drawing.Point(10, 340);
            this.lblSecretKey.Name = "lblSecretKey";
            this.lblSecretKey.Size = new System.Drawing.Size(59, 13);
            this.lblSecretKey.TabIndex = 6;
            this.lblSecretKey.Text = "Secret Key";
            // 
            // txtSecretKey
            // 
            this.txtSecretKey.Location = new System.Drawing.Point(89, 333);
            this.txtSecretKey.Name = "txtSecretKey";
            this.txtSecretKey.Size = new System.Drawing.Size(201, 20);
            this.txtSecretKey.TabIndex = 4;
            // 
            // cmbPictureCount
            // 
            this.cmbPictureCount.FormattingEnabled = true;
            this.cmbPictureCount.Items.AddRange(new object[] {
            "100",
            "200",
            "300",
            "400",
            "500",
            "600",
            "700",
            "800",
            "900"});
            this.cmbPictureCount.Location = new System.Drawing.Point(89, 384);
            this.cmbPictureCount.Name = "cmbPictureCount";
            this.cmbPictureCount.Size = new System.Drawing.Size(201, 21);
            this.cmbPictureCount.TabIndex = 6;
            this.cmbPictureCount.Text = "1000";
            // 
            // lblPictures
            // 
            this.lblPictures.AutoSize = true;
            this.lblPictures.Location = new System.Drawing.Point(10, 392);
            this.lblPictures.Name = "lblPictures";
            this.lblPictures.Size = new System.Drawing.Size(45, 13);
            this.lblPictures.TabIndex = 9;
            this.lblPictures.Text = "Pictures";
            // 
            // pictureBox1
            // 
            this.pictureBox1.Image = global::ecspics_loader.Properties.Resources.ecs_logo_1;
            this.pictureBox1.ImageLocation = "";
            this.pictureBox1.Location = new System.Drawing.Point(-4, 16);
            this.pictureBox1.Name = "pictureBox1";
            this.pictureBox1.Size = new System.Drawing.Size(313, 253);
            this.pictureBox1.TabIndex = 10;
            this.pictureBox1.TabStop = false;
            // 
            // lblIpPort
            // 
            this.lblIpPort.AutoSize = true;
            this.lblIpPort.Location = new System.Drawing.Point(10, 288);
            this.lblIpPort.Name = "lblIpPort";
            this.lblIpPort.Size = new System.Drawing.Size(41, 13);
            this.lblIpPort.TabIndex = 11;
            this.lblIpPort.Text = "IP/Port";
            // 
            // txtIpAddress
            // 
            this.txtIpAddress.Location = new System.Drawing.Point(89, 281);
            this.txtIpAddress.Name = "txtIpAddress";
            this.txtIpAddress.Size = new System.Drawing.Size(131, 20);
            this.txtIpAddress.TabIndex = 1;
            this.txtIpAddress.Text = "192.168.1.11";
            // 
            // cmbPort
            // 
            this.cmbPort.FormattingEnabled = true;
            this.cmbPort.Items.AddRange(new object[] {
            "9020",
            "9021",
            "443"});
            this.cmbPort.Location = new System.Drawing.Point(227, 281);
            this.cmbPort.Name = "cmbPort";
            this.cmbPort.Size = new System.Drawing.Size(63, 21);
            this.cmbPort.TabIndex = 2;
            this.cmbPort.Text = "9020";
            // 
            // Main
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(784, 466);
            this.Controls.Add(this.cmbPort);
            this.Controls.Add(this.txtIpAddress);
            this.Controls.Add(this.lblIpPort);
            this.Controls.Add(this.pictureBox1);
            this.Controls.Add(this.lblPictures);
            this.Controls.Add(this.cmbPictureCount);
            this.Controls.Add(this.txtSecretKey);
            this.Controls.Add(this.lblSecretKey);
            this.Controls.Add(this.txtUserName);
            this.Controls.Add(this.lblUserName);
            this.Controls.Add(this.txtStatus);
            this.Controls.Add(this.txtBucketName);
            this.Controls.Add(this.lblBucket);
            this.Controls.Add(this.cmdUpload);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedToolWindow;
            this.Name = "Main";
            this.Text = "Media Loader";
            ((System.ComponentModel.ISupportInitialize)(this.pictureBox1)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button cmdUpload;
        private System.Windows.Forms.Label lblBucket;
        private System.Windows.Forms.TextBox txtBucketName;
        private System.Windows.Forms.TextBox txtStatus;
        private System.Windows.Forms.Label lblUserName;
        private System.Windows.Forms.TextBox txtUserName;
        private System.Windows.Forms.Label lblSecretKey;
        private System.Windows.Forms.TextBox txtSecretKey;
        private System.Windows.Forms.Label lblPictures;
        private System.Windows.Forms.PictureBox pictureBox1;
        private System.Windows.Forms.Label lblIpPort;
        private System.Windows.Forms.TextBox txtIpAddress;
        private System.Windows.Forms.ComboBox cmbPort;
        internal System.Windows.Forms.ComboBox cmbPictureCount;
    }
}

